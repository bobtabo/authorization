package handler

import (
	uinvitation "authorization-go/internal/usecase/invitation"
	"net/http"

	"github.com/gin-gonic/gin"
)

type AdminInvitationHandler struct {
	svc *uinvitation.Interactor
}

func NewAdminInvitationHandler(svc *uinvitation.Interactor) *AdminInvitationHandler {
	return &AdminInvitationHandler{svc: svc}
}

// GET /api/admin/invitation
func (h *AdminInvitationHandler) Index(c *gin.Context) {
	result, err := h.svc.Current()
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"found":       true,
		"url":         result.URL,
		"display_url": result.DisplayURL,
		"token":       result.Token,
	})
}

// GET /api/admin/invitation/issue
func (h *AdminInvitationHandler) Issue(c *gin.Context) {
	result, err := h.svc.Issue()
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"found":       true,
		"url":         result.URL,
		"display_url": result.DisplayURL,
		"token":       result.Token,
	})
}
