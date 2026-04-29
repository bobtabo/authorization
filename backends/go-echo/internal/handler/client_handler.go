// Package handler はHTTPハンドラを提供します。
package handler

import (
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
	"gorm.io/gorm"
)

// ClientHandler はクライアント関連のHTTPハンドラです。
type ClientHandler struct {
	db          *gorm.DB
	newClientUC func(*gorm.DB) *uclient.Interactor
	newNotifUC  func(*gorm.DB) *unotification.Interactor
	mailer      *mail.Mailer
}

// NewClientHandler は ClientHandler を生成します。
func NewClientHandler(
	db *gorm.DB,
	newClientUC func(*gorm.DB) *uclient.Interactor,
	newNotifUC func(*gorm.DB) *unotification.Interactor,
	mailer *mail.Mailer,
) *ClientHandler {
	return &ClientHandler{
		db:          db,
		newClientUC: newClientUC,
		newNotifUC:  newNotifUC,
		mailer:      mailer,
	}
}

// Index はクライアント一覧を返します。
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

	clients, err := h.newClientUC(h.db).FindByCondition(cond)
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, mapClientList(clients))
}

// Show はクライアント詳細を返します。
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

// Store はクライアントを新規登録します。
func (h *ClientHandler) Store(c echo.Context) error {
	var body struct {
		Name     string `json:"name"`
		PostCode string `json:"post_code"`
		Pref     string `json:"pref"`
		City     string `json:"city"`
		Address  string `json:"address"`
		Building string `json:"building"`
		Tel      string `json:"tel"`
		Email    string `json:"email"`
	}
	if err := c.Bind(&body); err != nil || body.Name == "" {
		return apperror.BadRequest("validation_error")
	}

	executorID := staffIDFromCookie(c)

	var storeVo *domclient.StoreVo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
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
		return txErr
	}

	notifURL := fmt.Sprintf("/clients/show?id=%d", storeVo.ID)
	_ = h.newNotifUC(h.db).FanOut(unotification.FanOutDto{
		Title:       "新しいクライアントが登録されました",
		Message:     storeVo.Name,
		MessageType: 1,
		ExecutorID:  executorID,
		URL:         notifURL,
	})

	go h.mailer.SendAccessToken(storeVo.Email, storeVo.Name, storeVo.AccessToken)

	return c.JSON(http.StatusCreated, map[string]interface{}{"id": storeVo.ID})
}

// Update はクライアント情報を更新します。
func (h *ClientHandler) Update(c echo.Context) error {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}

	var body struct {
		Name     *string `json:"name"`
		PostCode *string `json:"post_code"`
		Pref     *string `json:"pref"`
		City     *string `json:"city"`
		Address  *string `json:"address"`
		Building *string `json:"building"`
		Tel      *string `json:"tel"`
		Email    *string `json:"email"`
		Status   *int    `json:"status"`
	}
	if err = c.Bind(&body); err != nil {
		return apperror.BadRequest("validation_error")
	}

	executorID := staffIDFromCookie(c)

	var detailVo *domclient.DetailVo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
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
		})
		return e
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, mapClientDetail(detailVo))
}

// Destroy はクライアントを論理削除します。
func (h *ClientHandler) Destroy(c echo.Context) error {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	executorID := staffIDFromCookie(c)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newClientUC(tx).Destroy(uclient.DestroyDto{ID: id, ExecutorID: executorID})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{})
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

func parseUint64Param(c echo.Context, key string) (uint64, error) {
	return strconv.ParseUint(c.Param(key), 10, 64)
}
