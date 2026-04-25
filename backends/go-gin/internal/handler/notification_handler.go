package handler

import (
	"authorization-go/internal/config"
	domnotification "authorization-go/internal/domain/notification"
	unotification "authorization-go/internal/usecase/notification"
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// NotificationHandler は通知関連のHTTPハンドラーを提供します。
type NotificationHandler struct {
	db         *gorm.DB
	newNotifUC func(*gorm.DB) *unotification.Interactor
	cfg        *config.Config
}

// NewNotificationHandler は NotificationHandler を生成します。
//
// db: GORM DB インスタンス
// newNotifUC: 通知ユースケースファクトリ
// cfg: アプリケーション設定
func NewNotificationHandler(db *gorm.DB, newNotifUC func(*gorm.DB) *unotification.Interactor, cfg *config.Config) *NotificationHandler {
	return &NotificationHandler{db: db, newNotifUC: newNotifUC, cfg: cfg}
}

// Counts はスタッフの未読・全体通知数を返します。
// GET /api/notifications/counts
func (h *NotificationHandler) Counts(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	unread, total, err := h.newNotifUC(h.db).Counts(staffID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"unread": unread, "total": total})
}

// Index はカーソルページングで通知一覧を返します。
// GET /api/notifications
func (h *NotificationHandler) Index(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}

	var cursor *string
	if v := c.Query("cursor"); v != "" {
		cursor = &v
	}

	limit := h.cfg.App.NotificationDefaultLimit
	if v := c.Query("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}

	page, err := h.newNotifUC(h.db).ListPage(staffID, cursor, limit)
	if err != nil {
		_ = c.Error(err)
		return
	}

	c.JSON(http.StatusOK, gin.H{"items": mapNotificationItems(page.Items), "next_cursor": page.NextCursor})
}

// ReadAll はスタッフの全通知を既読にして更新件数を返します。
// PATCH /api/notifications
func (h *NotificationHandler) ReadAll(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	var updated int64
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		updated, e = h.newNotifUC(tx).BulkMarkRead(staffID)
		return e
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{"updated": updated})
}

// Read は通知を既読にします。
// PATCH /api/notifications/:id
func (h *NotificationHandler) Read(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil || id <= 0 {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newNotifUC(tx).MarkRead(id)
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// ---------- 変換ヘルパー ----------

// mapNotificationItems は通知 Item Vo スライスをレスポンス用マップのスライスに変換します。
func mapNotificationItems(items []*domnotification.Item) []gin.H {
	out := make([]gin.H, 0, len(items))
	for _, n := range items {
		out = append(out, gin.H{
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
