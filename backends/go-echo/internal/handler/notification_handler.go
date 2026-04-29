// Package handler はHTTPハンドラを提供します。
package handler

import (
	"authorization-go-echo/internal/config"
	domnotification "authorization-go-echo/internal/domain/notification"
	unotification "authorization-go-echo/internal/usecase/notification"
	"authorization-go-echo/pkg/apperror"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

// NotificationHandler は通知関連のHTTPハンドラです。
type NotificationHandler struct {
	db         *gorm.DB
	newNotifUC func(*gorm.DB) *unotification.Interactor
	cfg        *config.Config
}

// NewNotificationHandler は NotificationHandler を生成します。
func NewNotificationHandler(db *gorm.DB, newNotifUC func(*gorm.DB) *unotification.Interactor, cfg *config.Config) *NotificationHandler {
	return &NotificationHandler{db: db, newNotifUC: newNotifUC, cfg: cfg}
}

// Counts は未読件数と総件数を返します。
func (h *NotificationHandler) Counts(c echo.Context) error {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		return apperror.Unauthorized("unauthenticated")
	}
	unread, total, err := h.newNotifUC(h.db).Counts(unotification.CountsDto{StaffID: staffID})
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"unread": unread, "total": total})
}

// Index は通知一覧をカーソルページングで返します。
func (h *NotificationHandler) Index(c echo.Context) error {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		return apperror.Unauthorized("unauthenticated")
	}

	var cursor *string
	if v := c.QueryParam("cursor"); v != "" {
		cursor = &v
	}

	limit := h.cfg.App.NotificationDefaultLimit
	if v := c.QueryParam("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}

	page, err := h.newNotifUC(h.db).ListPage(unotification.ListPageDto{StaffID: staffID, Cursor: cursor, Limit: limit})
	if err != nil {
		return err
	}

	return c.JSON(http.StatusOK, map[string]interface{}{
		"items":       mapNotificationItems(page.Items),
		"next_cursor": page.NextCursor,
	})
}

// ReadAll は全通知を一括既読にします。
func (h *NotificationHandler) ReadAll(c echo.Context) error {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		return apperror.Unauthorized("unauthenticated")
	}
	var updated int64
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		updated, e = h.newNotifUC(tx).BulkMarkRead(unotification.BulkMarkReadDto{StaffID: staffID})
		return e
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"updated": updated})
}

// Read は指定した通知を既読にします。
func (h *NotificationHandler) Read(c echo.Context) error {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil || id <= 0 {
		return apperror.BadRequest("invalid_id")
	}
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newNotifUC(tx).MarkRead(unotification.MarkReadDto{ID: id})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"id": id})
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
