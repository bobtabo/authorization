package handler

import (
	domclient "authorization-go-beego/internal/domain/client"
	"authorization-go-beego/internal/infrastructure/mail"
	"authorization-go-beego/internal/infrastructure/persistence"
	uclient "authorization-go-beego/internal/usecase/client"
	unotification "authorization-go-beego/internal/usecase/notification"
	"authorization-go-beego/pkg/apperror"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"time"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"github.com/beego/beego/v2/client/orm"
)

type ClientHandler struct {
	ormer       orm.Ormer
	newClientUC func(persistence.QueryOrmer) *uclient.Interactor
	newNotifUC  func(persistence.QueryOrmer) *unotification.Interactor
	mailer      *mail.Mailer
	historyRepo domclient.JwtHistoryRepository
	frontendURL string
}

func NewClientHandler(
	ormer orm.Ormer,
	newClientUC func(persistence.QueryOrmer) *uclient.Interactor,
	newNotifUC func(persistence.QueryOrmer) *unotification.Interactor,
	mailer *mail.Mailer,
	historyRepo domclient.JwtHistoryRepository,
	frontendURL string,
) *ClientHandler {
	return &ClientHandler{
		ormer:       ormer,
		newClientUC: newClientUC,
		newNotifUC:  newNotifUC,
		mailer:      mailer,
		historyRepo: historyRepo,
		frontendURL: frontendURL,
	}
}

func (h *ClientHandler) Index(ctx *beecontext.Context) {
	cond := domclient.Condition{}

	if kw := ctx.Input.Query("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	if v := ctx.Input.Query("start_from"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			cond.StartFrom = &t
		}
	}
	if v := ctx.Input.Query("start_to"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			cond.StartTo = &t
		}
	}

	limit := 10
	if v := ctx.Input.Query("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}
	if limit > 500 {
		limit = 500
	}
	page := 1
	if v := ctx.Input.Query("page"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			page = n
		}
	}
	offset := limit * (page - 1)
	cond.Offset = offset
	cond.Limit = limit
	cond.Sort = ctx.Input.Query("sort")
	cond.SortType = ctx.Input.Query("sort_type")

	uc := h.newClientUC(h.ormer)
	count, err := uc.CountByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}
	clients, err := uc.FindByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}

	pager := BuildPager(count, limit, offset, len(clients))
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"data":  mapClientList(clients),
		"pager": pager,
	})
}

func (h *ClientHandler) Show(ctx *beecontext.Context) {
	id, err := strconv.ParseUint(ctx.Input.Param(":id"), 10, 64)
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	client, err := h.newClientUC(h.ormer).FindByID(uclient.FindByIDDto{ID: id})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, mapClientDetail(client))
}

func (h *ClientHandler) Store(ctx *beecontext.Context) {
	var body StoreClientBody
	if err := json.Unmarshal(ctx.Input.RequestBody, &body); err != nil {
		writeError(ctx, apperror.UnprocessableEntity("validation_error"))
		return
	}
	if err := validateStruct(&body); err != nil {
		writeError(ctx, err)
		return
	}

	executorID := staffIDFromCookie(ctx)

	var storeVo *domclient.StoreVo
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		var e error
		storeVo, e = h.newClientUC(tx).Store(uclient.StoreDto{
			Name:       body.Name,
			PostCode:   body.PostCode,
			Pref:       body.Pref,
			City:       body.City,
			Address:    body.Address,
			Building:   body.Building,
			Tel:        body.Tel,
			Email:      body.Email,
			ExecutorID: executorID,
		})
		return e
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}

	notifURL := fmt.Sprintf("/clients/show?id=%d", storeVo.ID)
	_ = h.newNotifUC(h.ormer).FanOut(unotification.FanOutDto{
		Title:       "新しいクライアントが登録されました",
		Message:     storeVo.Name,
		MessageType: 1,
		ExecutorID:  executorID,
		URL:         notifURL,
	})

	activateURL := h.frontendURL + "/clients/" + storeVo.Identifier + "/qr"
	go h.mailer.SendActivation(storeVo.Email, storeVo.Name, activateURL)

	writeJSON(ctx, http.StatusCreated, map[string]interface{}{"id": storeVo.ID})
}

func (h *ClientHandler) Update(ctx *beecontext.Context) {
	id, err := strconv.ParseUint(ctx.Input.Param(":id"), 10, 64)
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}

	var body UpdateClientBody
	if err = json.Unmarshal(ctx.Input.RequestBody, &body); err != nil {
		writeError(ctx, apperror.UnprocessableEntity("validation_error"))
		return
	}
	if err = validateStruct(&body); err != nil {
		writeError(ctx, err)
		return
	}

	executorID := staffIDFromCookie(ctx)

	var detailVo *domclient.DetailVo
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		var e error
		detailVo, e = h.newClientUC(tx).Update(uclient.UpdateDto{
			ID:         id,
			Name:       body.Name,
			PostCode:   body.PostCode,
			Pref:       body.Pref,
			City:       body.City,
			Address:    body.Address,
			Building:   body.Building,
			Tel:        body.Tel,
			Email:      body.Email,
			Status:     body.Status,
			ExecutorID: executorID,
			Version:    body.Version,
		})
		return e
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, mapClientDetail(detailVo))
}

func (h *ClientHandler) GetQr(ctx *beecontext.Context) {
	identifier := ctx.Input.Param(":identifier")
	vo, err := h.newClientUC(h.ormer).GetQr(uclient.FindByIdentifierDto{Identifier: identifier})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"identifier":   vo.Identifier,
		"deeplink_url": vo.DeeplinkURL,
	})
}

func (h *ClientHandler) GetInfo(ctx *beecontext.Context) {
	identifier := ctx.Input.Param(":identifier")
	vo, err := h.newClientUC(h.ormer).GetInfo(uclient.FindByIdentifierDto{Identifier: identifier})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"identifier": vo.Identifier,
		"name":       vo.Name,
		"status":     vo.Status,
	})
}

func (h *ClientHandler) Start(ctx *beecontext.Context) {
	identifier := ctx.Input.Param(":identifier")
	var vo *domclient.StartVo
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		var e error
		vo, e = h.newClientUC(tx).Start(uclient.FindByIdentifierDto{Identifier: identifier})
		return e
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"access_token": vo.AccessToken,
	})
}

func (h *ClientHandler) Stop(ctx *beecontext.Context) {
	identifier := ctx.Input.Param(":identifier")
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		return h.newClientUC(tx).Stop(uclient.FindByIdentifierDto{Identifier: identifier})
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{})
}

func (h *ClientHandler) Destroy(ctx *beecontext.Context) {
	id, err := strconv.ParseUint(ctx.Input.Param(":id"), 10, 64)
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(ctx)
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		return h.newClientUC(tx).Destroy(uclient.DestroyDto{ID: id, ExecutorID: executorID})
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{})
}

func (h *ClientHandler) JwtHistories(ctx *beecontext.Context) {
	idStr := ctx.Input.Param(":id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}

	limit := 10
	if v := ctx.Input.Query("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}
	if limit > 500 {
		limit = 500
	}
	page := 1
	if v := ctx.Input.Query("page"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			page = n
		}
	}
	offset := limit * (page - 1)
	cond := domclient.JwtHistoryCondition{
		ClientID: id,
		Offset:   offset,
		Limit:    limit,
		Sort:     ctx.Input.Query("sort"),
		SortType: ctx.Input.Query("sort_type"),
	}

	count, err := h.historyRepo.CountByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}
	histories, err := h.historyRepo.FindByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}
	out := make([]map[string]interface{}, 0, len(histories))
	for _, hist := range histories {
		out = append(out, map[string]interface{}{
			"id":        hist.ID,
			"member_id": hist.MemberID,
			"issue_at":  hist.IssueAt.Format("2006-01-02 15:04:05"),
			"jwt":       hist.Jwt,
		})
	}
	pager := BuildPager(count, limit, offset, len(histories))
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"data": out, "pager": pager})
}

func mapClientList(clients []*domclient.ListItem) []map[string]interface{} {
	out := make([]map[string]interface{}, 0, len(clients))
	for _, c := range clients {
		out = append(out, map[string]interface{}{
			"id":         c.ID,
			"name":       c.Name,
			"status":     c.Status,
			"start_at":   formatTimePtr(c.StartAt),
			"stop_at":    formatTimePtr(c.StopAt),
			"created_at": formatTime(c.CreatedAt),
			"updated_at": formatTime(c.UpdatedAt),
		})
	}
	return out
}

func mapClientDetail(c *domclient.DetailVo) map[string]interface{} {
	return map[string]interface{}{
		"id":         c.ID,
		"name":       c.Name,
		"identifier": c.Identifier,
		"post_code":  c.PostCode,
		"pref":       c.Pref,
		"city":       c.City,
		"address":    c.Address,
		"building":   c.Building,
		"tel":        c.Tel,
		"email":      c.Email,
		"status":     c.Status,
		"start_at":   formatTimePtr(c.StartAt),
		"stop_at":    formatTimePtr(c.StopAt),
		"created_at": formatTime(c.CreatedAt),
		"updated_at": formatTime(c.UpdatedAt),
	}
}
