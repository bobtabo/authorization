package handler

import (
	"authorization-go-echo/ent"
	domclient "authorization-go-echo/internal/domain/client"
	"authorization-go-echo/internal/infrastructure/mail"
	uclient "authorization-go-echo/internal/usecase/client"
	unotification "authorization-go-echo/internal/usecase/notification"
	"authorization-go-echo/pkg/apperror"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"github.com/labstack/echo/v4"
)

type ClientHandler struct {
	db          *ent.Client
	newClientUC func(*ent.Client) *uclient.Interactor
	newNotifUC  func(*ent.Client) *unotification.Interactor
	mailer      *mail.Mailer
	historyRepo domclient.JwtHistoryRepository
	frontendURL string
}

func NewClientHandler(
	db *ent.Client,
	newClientUC func(*ent.Client) *uclient.Interactor,
	newNotifUC func(*ent.Client) *unotification.Interactor,
	mailer *mail.Mailer,
	historyRepo domclient.JwtHistoryRepository,
	frontendURL string,
) *ClientHandler {
	return &ClientHandler{db: db, newClientUC: newClientUC, newNotifUC: newNotifUC, mailer: mailer, historyRepo: historyRepo, frontendURL: frontendURL}
}

func (h *ClientHandler) Index(c echo.Context) error {
	cond := domclient.Condition{}
	if kw := c.QueryParam("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	if v := c.QueryParam("start_from"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			cond.StartFrom = &t
		}
	}
	if v := c.QueryParam("start_to"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			cond.StartTo = &t
		}
	}

	limit := 10
	if v := c.QueryParam("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}
	page := 1
	if v := c.QueryParam("page"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			page = n
		}
	}
	offset := limit * (page - 1)
	cond.Offset = offset
	cond.Limit = limit
	cond.Sort = c.QueryParam("sort")
	cond.SortType = c.QueryParam("sort_type")

	uc := h.newClientUC(h.db)
	count, err := uc.CountByCondition(cond)
	if err != nil {
		return err
	}
	clients, err := uc.FindByCondition(cond)
	if err != nil {
		return err
	}

	pager := BuildPager(count, limit, offset, len(clients))
	return c.JSON(http.StatusOK, map[string]interface{}{
		"data":  mapClientList(clients),
		"pager": pager,
	})
}

func (h *ClientHandler) Show(c echo.Context) error {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	client, err := h.newClientUC(h.db).FindByID(uclient.FindByIDDto{ID: id})
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, mapClientDetail(client))
}

func (h *ClientHandler) Store(c echo.Context) error {
	var body StoreClientBody
	if err := c.Bind(&body); err != nil {
		return apperror.UnprocessableEntity("validation_error")
	}
	if err := validateStruct(&body); err != nil {
		return err
	}
	executorID := staffIDFromCookie(c)
	var storeVo *domclient.StoreVo
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		var e error
		storeVo, e = h.newClientUC(tx.Client()).Store(uclient.StoreDto{
			Name: body.Name, PostCode: body.PostCode, Pref: body.Pref,
			City: body.City, Address: body.Address, Building: body.Building,
			Tel: body.Tel, Email: body.Email, ExecutorID: executorID,
		})
		return e
	}); txErr != nil {
		return txErr
	}
	notifURL := fmt.Sprintf("/clients/show?id=%d", storeVo.ID)
	_ = h.newNotifUC(h.db).FanOut(unotification.FanOutDto{
		Title: "新しいクライアントが登録されました", Message: storeVo.Name,
		MessageType: 1, ExecutorID: executorID, URL: notifURL,
	})
	activateURL := h.frontendURL + "/clients/" + storeVo.Identifier + "/qr"
	go h.mailer.SendActivation(storeVo.Email, storeVo.Name, activateURL)
	return c.JSON(http.StatusCreated, map[string]interface{}{"id": storeVo.ID})
}

func (h *ClientHandler) Update(c echo.Context) error {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	var body UpdateClientBody
	if err = c.Bind(&body); err != nil {
		return apperror.UnprocessableEntity("validation_error")
	}
	if err = validateStruct(&body); err != nil {
		return err
	}
	executorID := staffIDFromCookie(c)
	var detailVo *domclient.DetailVo
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		var e error
		detailVo, e = h.newClientUC(tx.Client()).Update(uclient.UpdateDto{
			ID: id, Name: body.Name, PostCode: body.PostCode, Pref: body.Pref,
			City: body.City, Address: body.Address, Building: body.Building,
			Tel: body.Tel, Email: body.Email, Status: body.Status, ExecutorID: executorID,
			Version: body.Version,
		})
		return e
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, mapClientDetail(detailVo))
}

func (h *ClientHandler) GetQr(c echo.Context) error {
	identifier := c.Param("identifier")
	vo, err := h.newClientUC(h.db).GetQr(uclient.FindByIdentifierDto{Identifier: identifier})
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, map[string]interface{}{
		"identifier":   vo.Identifier,
		"deeplink_url": vo.DeeplinkURL,
	})
}

func (h *ClientHandler) GetInfo(c echo.Context) error {
	identifier := c.Param("identifier")
	vo, err := h.newClientUC(h.db).GetInfo(uclient.FindByIdentifierDto{Identifier: identifier})
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, map[string]interface{}{
		"identifier": vo.Identifier,
		"name":       vo.Name,
		"status":     vo.Status,
	})
}

func (h *ClientHandler) Start(c echo.Context) error {
	identifier := c.Param("identifier")
	var vo *domclient.StartVo
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		var e error
		vo, e = h.newClientUC(tx.Client()).Start(uclient.FindByIdentifierDto{Identifier: identifier})
		return e
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{
		"access_token": vo.AccessToken,
	})
}

func (h *ClientHandler) Stop(c echo.Context) error {
	identifier := c.Param("identifier")
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		return h.newClientUC(tx.Client()).Stop(uclient.FindByIdentifierDto{Identifier: identifier})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{})
}

func (h *ClientHandler) Destroy(c echo.Context) error {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	var body struct {
		Version int `json:"version"`
	}
	if err = c.Bind(&body); err != nil {
		return apperror.BadRequest("validation_error")
	}
	executorID := staffIDFromCookie(c)
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		return h.newClientUC(tx.Client()).Destroy(uclient.DestroyDto{ID: id, ExecutorID: executorID, Version: body.Version})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{})
}

const timeFormatSec = "2006-01-02 15:04:05"

func (h *ClientHandler) JwtHistories(c echo.Context) error {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}

	limit := 10
	if v := c.QueryParam("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}
	page := 1
	if v := c.QueryParam("page"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			page = n
		}
	}
	offset := limit * (page - 1)
	cond := domclient.JwtHistoryCondition{
		ClientID: id,
		Offset:   offset,
		Limit:    limit,
		Sort:     c.QueryParam("sort"),
		SortType: c.QueryParam("sort_type"),
	}

	count, err := h.historyRepo.CountByCondition(cond)
	if err != nil {
		return err
	}
	histories, err := h.historyRepo.FindByCondition(cond)
	if err != nil {
		return err
	}
	out := make([]map[string]interface{}, 0, len(histories))
	for _, hist := range histories {
		out = append(out, map[string]interface{}{
			"id":        hist.ID,
			"member_id": hist.MemberID,
			"issue_at":  hist.IssueAt.Format(timeFormatSec),
			"jwt":       hist.Jwt,
		})
	}
	pager := BuildPager(count, limit, offset, len(histories))
	return c.JSON(http.StatusOK, map[string]interface{}{"data": out, "pager": pager})
}

func mapClientList(clients []*domclient.ListItem) []map[string]interface{} {
	out := make([]map[string]interface{}, 0, len(clients))
	for _, c := range clients {
		out = append(out, map[string]interface{}{
			"id": c.ID, "name": c.Name, "status": c.Status,
			"start_at": formatTimePtr(c.StartAt), "stop_at": formatTimePtr(c.StopAt),
			"created_at": formatTime(c.CreatedAt), "updated_at": formatTime(c.UpdatedAt),
		})
	}
	return out
}

func mapClientDetail(c *domclient.DetailVo) map[string]interface{} {
	return map[string]interface{}{
		"id": c.ID, "name": c.Name, "identifier": c.Identifier,
		"post_code": c.PostCode, "pref": c.Pref, "city": c.City,
		"address": c.Address, "building": c.Building, "tel": c.Tel, "email": c.Email,
		"status": c.Status, "start_at": formatTimePtr(c.StartAt), "stop_at": formatTimePtr(c.StopAt),
		"created_at": formatTime(c.CreatedAt), "updated_at": formatTime(c.UpdatedAt),
	}
}

func parseUint64Param(c echo.Context, key string) (uint64, error) {
	return strconv.ParseUint(c.Param(key), 10, 64)
}
