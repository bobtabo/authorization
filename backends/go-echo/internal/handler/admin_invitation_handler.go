package handler

import (
	dominvitation "authorization-go-echo/internal/domain/invitation"
	uinvitation "authorization-go-echo/internal/usecase/invitation"
	"net/http"

	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

type AdminInvitationHandler struct {
	db          *gorm.DB
	newInviteUC func(*gorm.DB) *uinvitation.Interactor
}

func NewAdminInvitationHandler(db *gorm.DB, newInviteUC func(*gorm.DB) *uinvitation.Interactor) *AdminInvitationHandler {
	return &AdminInvitationHandler{db: db, newInviteUC: newInviteUC}
}

func (h *AdminInvitationHandler) Index(c echo.Context) error {
	result, err := h.newInviteUC(h.db).Current()
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, mapInvitationVo(result))
}

func (h *AdminInvitationHandler) Issue(c echo.Context) error {
	var result *dominvitation.Vo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		result, e = h.newInviteUC(tx).Issue()
		return e
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, mapInvitationVo(result))
}

func mapInvitationVo(v *dominvitation.Vo) map[string]interface{} {
	return map[string]interface{}{
		"found":       true,
		"url":         v.URL,
		"display_url": v.DisplayURL,
		"token":       v.Token,
	}
}
