package handler

import (
	"authorization-go-beego/internal/config"
	domnotification "authorization-go-beego/internal/domain/notification"
	unotification "authorization-go-beego/internal/usecase/notification"
	"authorization-go-beego/pkg/apperror"
	"net/http"
	"strconv"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"gorm.io/gorm"
)

type NotificationHandler struct {
	db         *gorm.DB
	newNotifUC func(*gorm.DB) *unotification.Interactor
	cfg        *config.Config
}

func NewNotificationHandler(db *gorm.DB, newNotifUC func(*gorm.DB) *unotification.Interactor, cfg *config.Config) *NotificationHandler {
	return &NotificationHandler{db: db, newNotifUC: newNotifUC, cfg: cfg}
}

func (h *NotificationHandler) Counts(ctx *beecontext.Context) {
	staffID := staffIDFromCookie(ctx)
	if staffID == 0 {
		writeError(ctx, apperror.Unauthorized("unauthenticated"))
		return
	}
	unread, total, err := h.newNotifUC(h.db).Counts(staffID)
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"unread": unread, "total": total})
}

func (h *NotificationHandler) Index(ctx *beecontext.Context) {
	staffID := staffIDFromCookie(ctx)
	if staffID == 0 {
		writeError(ctx, apperror.Unauthorized("unauthenticated"))
		return
	}

	var cursor *string
	if v := ctx.Input.Query("cursor"); v != "" {
		cursor = &v
	}

	limit := h.cfg.App.NotificationDefaultLimit
	if v := ctx.Input.Query("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}

	page, err := h.newNotifUC(h.db).ListPage(staffID, cursor, limit)
	if err != nil {
		writeError(ctx, err)
		return
	}

	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"items":       mapNotificationItems(page.Items),
		"next_cursor": page.NextCursor,
	})
}

func (h *NotificationHandler) ReadAll(ctx *beecontext.Context) {
	staffID := staffIDFromCookie(ctx)
	if staffID == 0 {
		writeError(ctx, apperror.Unauthorized("unauthenticated"))
		return
	}
	var updated int64
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		updated, e = h.newNotifUC(tx).BulkMarkRead(staffID)
		return e
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"updated": updated})
}

func (h *NotificationHandler) Read(ctx *beecontext.Context) {
	id, err := strconv.ParseInt(ctx.Input.Param(":id"), 10, 64)
	if err != nil || id <= 0 {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newNotifUC(tx).MarkRead(id)
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"id": id})
}

func mapNotificationItems(items []*domnotification.Item) []map[string]interface{} {
	out := make([]map[string]interface{}, 0, len(items))
	for _, n := range items {
		out = append(out, map[string]interface{}{
			"id":           n.ID,
			"staff_id":     n.StaffID,
			"message_type": n.MessageType,
			"title":        n.Title,
			"message":      n.Message,
			"url":          n.URL,
			"read":         n.Read,
			"created_at":   n.CreatedAt,
			"updated_at":   n.UpdatedAt,
		})
	}
	return out
}
