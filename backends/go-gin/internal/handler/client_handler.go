package handler

import (
	"authorization-go/internal/model"
	"authorization-go/internal/repository"
	"authorization-go/internal/service"
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

type ClientHandler struct {
	clientSvc       *service.ClientService
	notificationSvc *service.NotificationService
}

func NewClientHandler(clientSvc *service.ClientService, notificationSvc *service.NotificationService) *ClientHandler {
	return &ClientHandler{clientSvc: clientSvc, notificationSvc: notificationSvc}
}

// GET /api/clients
func (h *ClientHandler) Index(c *gin.Context) {
	f := repository.ClientFilter{}

	if kw := c.Query("keyword"); kw != "" {
		f.Keyword = &kw
	}
	if v := c.Query("start_from"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			f.StartFrom = &t
		}
	}
	if v := c.Query("start_to"); v != "" {
		if t, err := time.Parse("2006-01-02", v); err == nil {
			f.StartTo = &t
		}
	}

	clients, err := h.clientSvc.FindByCondition(f)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapClientList(clients))
}

// GET /api/clients/:id
func (h *ClientHandler) Show(c *gin.Context) {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	client, err := h.clientSvc.FindByID(id)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapClientDetail(client))
}

// POST /api/clients/store
func (h *ClientHandler) Store(c *gin.Context) {
	var body struct {
		Name     string `json:"name"     binding:"required"`
		PostCode string `json:"post_code"`
		Pref     string `json:"pref"`
		City     string `json:"city"`
		Address  string `json:"address"`
		Building string `json:"building"`
		Tel      string `json:"tel"`
		Email    string `json:"email"`
	}
	if err := c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("validation_error"))
		return
	}

	executorID := staffIDFromCookie(c)

	client, err := h.clientSvc.Store(service.StoreClientInput{
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
	if err != nil {
		_ = c.Error(err)
		return
	}

	// 全スタッフへ通知配信
	_ = h.notificationSvc.FanOut("新しいクライアントが登録されました", client.Name, 1, executorID)

	c.JSON(http.StatusCreated, gin.H{"id": client.ID})
}

// PUT /api/clients/:id/update
func (h *ClientHandler) Update(c *gin.Context) {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
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
	if err = c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("validation_error"))
		return
	}

	executorID := staffIDFromCookie(c)

	client, err := h.clientSvc.Update(service.UpdateClientInput{
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
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapClientDetail(client))
}

// DELETE /api/clients/:id/delete
func (h *ClientHandler) Destroy(c *gin.Context) {
	id, err := parseUint64Param(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(c)
	if err = h.clientSvc.Destroy(id, executorID); err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{})
}

// ---------- 変換ヘルパー ----------

func mapClientList(clients []*model.Client) []gin.H {
	out := make([]gin.H, 0, len(clients))
	for _, c := range clients {
		out = append(out, gin.H{
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

func mapClientDetail(c *model.Client) gin.H {
	return gin.H{
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

func parseUint64Param(c *gin.Context, key string) (uint64, error) {
	return strconv.ParseUint(c.Param(key), 10, 64)
}
