package handler

import (
	"authorization-go/internal/config"
	"authorization-go/internal/service"
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type NotificationHandler struct {
	svc *service.NotificationService
	cfg *config.Config
}

func NewNotificationHandler(svc *service.NotificationService, cfg *config.Config) *NotificationHandler {
	return &NotificationHandler{svc: svc, cfg: cfg}
}

// GET /api/notifications/counts
func (h *NotificationHandler) Counts(c *gin.Context) {
	staffID := staffIDFromCookie(c)
	if staffID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	unread, total, err := h.svc.Counts(staffID)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"unread": unread, "total": total})
}

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

	page, err := h.svc.ListPage(staffID, cursor, limit)
	if err != nil {
		_ = c.Error(err)
		return
	}

	items := make([]map[string]interface{}, 0, len(page.Items))
	for _, n := range page.Items {
		items = append(items, service.MapNotification(n))
	}
	c.JSON(http.StatusOK, gin.H{"items": items, "next_cursor": page.NextCursor})
}

// POST /api/notifications
func (h *NotificationHandler) Store(c *gin.Context) {
	var body interface{}
	_ = c.ShouldBindJSON(&body)
	c.JSON(http.StatusAccepted, gin.H{
		"message":  "notification_accepted",
		"received": body,
	})
}

// PATCH /api/notifications  (一括既読)
func (h *NotificationHandler) ReadAll(c *gin.Context) {
	var body struct {
		IDs        []int64 `json:"ids"`
		All        bool    `json:"all"`
		ExecutorID int64   `json:"executor_id"`
	}
	if err := c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("invalid_request"))
		return
	}
	if body.ExecutorID == 0 {
		_ = c.Error(apperror.Unauthorized("unauthenticated"))
		return
	}
	if len(body.IDs) == 0 && !body.All {
		_ = c.Error(apperror.BadRequest("ids_or_all_required"))
		return
	}

	updated, err := h.svc.BulkMarkRead(body.ExecutorID, body.IDs, body.All)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"updated": updated})
}

// PATCH /api/notifications/:id
func (h *NotificationHandler) Read(c *gin.Context) {
	id, err := strconv.ParseInt(c.Param("id"), 10, 64)
	if err != nil || id <= 0 {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}

	var body map[string]interface{}
	if err = c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("invalid_request"))
		return
	}

	if err = h.svc.Patch(id, body); err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}
