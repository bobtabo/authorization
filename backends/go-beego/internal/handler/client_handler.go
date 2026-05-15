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
}

func NewClientHandler(
	ormer orm.Ormer,
	newClientUC func(persistence.QueryOrmer) *uclient.Interactor,
	newNotifUC func(persistence.QueryOrmer) *unotification.Interactor,
	mailer *mail.Mailer,
) *ClientHandler {
	return &ClientHandler{
		ormer:       ormer,
		newClientUC: newClientUC,
		newNotifUC:  newNotifUC,
		mailer:      mailer,
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

	clients, err := h.newClientUC(h.ormer).FindByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, mapClientList(clients))
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
	if err := json.Unmarshal(ctx.Input.RequestBody, &body); err != nil || body.Name == "" {
		writeError(ctx, apperror.BadRequest("validation_error"))
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

	go h.mailer.SendAccessToken(storeVo.Email, storeVo.Name, storeVo.AccessToken)

	writeJSON(ctx, http.StatusCreated, map[string]interface{}{"id": storeVo.ID})
}

func (h *ClientHandler) Update(ctx *beecontext.Context) {
	id, err := strconv.ParseUint(ctx.Input.Param(":id"), 10, 64)
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
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
	if err = json.Unmarshal(ctx.Input.RequestBody, &body); err != nil {
		writeError(ctx, apperror.BadRequest("validation_error"))
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
