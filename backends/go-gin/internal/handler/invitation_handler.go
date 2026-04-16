package handler

import (
	uinvitation "authorization-go/internal/usecase/invitation"
	"net/http"

	"github.com/gin-gonic/gin"
)

type InvitationHandler struct {
	svc *uinvitation.Interactor
}

func NewInvitationHandler(svc *uinvitation.Interactor) *InvitationHandler {
	return &InvitationHandler{svc: svc}
}

// GET /api/invitation
func (h *InvitationHandler) Index(c *gin.Context) {
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

// GET /api/invitation/issue
func (h *InvitationHandler) Issue(c *gin.Context) {
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
