package handler

import (
	dominvitation "authorization-go-beego/internal/domain/invitation"
	"authorization-go-beego/internal/infrastructure/persistence"
	uinvitation "authorization-go-beego/internal/usecase/invitation"
	"context"
	"net/http"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"github.com/beego/beego/v2/client/orm"
)

type AdminInvitationHandler struct {
	ormer       orm.Ormer
	newInviteUC func(persistence.QueryOrmer) *uinvitation.Interactor
}

func NewAdminInvitationHandler(ormer orm.Ormer, newInviteUC func(persistence.QueryOrmer) *uinvitation.Interactor) *AdminInvitationHandler {
	return &AdminInvitationHandler{ormer: ormer, newInviteUC: newInviteUC}
}

func (h *AdminInvitationHandler) Index(ctx *beecontext.Context) {
	result, err := h.newInviteUC(h.ormer).Current()
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, mapInvitationVo(result))
}

func (h *AdminInvitationHandler) Issue(ctx *beecontext.Context) {
	var result *dominvitation.Vo
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
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
