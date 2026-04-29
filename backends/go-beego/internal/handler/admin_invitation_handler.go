package handler

import (
	dominvitation "authorization-go-beego/internal/domain/invitation"
	uinvitation "authorization-go-beego/internal/usecase/invitation"
	"net/http"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"gorm.io/gorm"
)

type AdminInvitationHandler struct {
	db          *gorm.DB
	newInviteUC func(*gorm.DB) *uinvitation.Interactor
}

func NewAdminInvitationHandler(db *gorm.DB, newInviteUC func(*gorm.DB) *uinvitation.Interactor) *AdminInvitationHandler {
	return &AdminInvitationHandler{db: db, newInviteUC: newInviteUC}
}

func (h *AdminInvitationHandler) Index(ctx *beecontext.Context) {
	result, err := h.newInviteUC(h.db).Current()
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, mapInvitationVo(result))
}

func (h *AdminInvitationHandler) Issue(ctx *beecontext.Context) {
	var result *dominvitation.Vo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		result, e = h.newInviteUC(tx).Issue()
		return e
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, mapInvitationVo(result))
}

func mapInvitationVo(v *dominvitation.Vo) map[string]interface{} {
	return map[string]interface{}{
		"found":       true,
		"url":         v.URL,
		"display_url": v.DisplayURL,
		"token":       v.Token,
	}
}
