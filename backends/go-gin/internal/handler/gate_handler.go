package handler

import (
	"authorization-go/internal/service"
	"authorization-go/pkg/apperror"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

type GateHandler struct {
	gateSvc *service.GateService
}

func NewGateHandler(gateSvc *service.GateService) *GateHandler {
	return &GateHandler{gateSvc: gateSvc}
}

// GET /api/gate/issue   (client.token ミドルウェア適用済み)
func (h *GateHandler) Issue(c *gin.Context) {
	member := c.Query("member")
	if member == "" {
		_ = c.Error(apperror.BadRequest("member_required"))
		return
	}

	auth := c.GetHeader("Authorization")
	accessToken := strings.TrimPrefix(auth, "Bearer ")

	token, err := h.gateSvc.IssueToken(accessToken, member)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"token": token})
}

// GET /api/gate/client/:identifier/verify
func (h *GateHandler) Verify(c *gin.Context) {
	identifier := c.Param("identifier")
	token := c.Query("token")
	if token == "" {
		_ = c.Error(apperror.BadRequest("token_required"))
		return
	}

	payload, err := h.gateSvc.Verify(identifier, token)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, payload)
}
