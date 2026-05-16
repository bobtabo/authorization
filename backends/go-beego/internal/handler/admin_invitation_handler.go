package handler

import (
	dominvitation "authorization-go-beego/internal/domain/invitation"
	"authorization-go-beego/internal/infrastructure/persistence"
	uinvitation "authorization-go-beego/internal/usecase/invitation"
	"authorization-go-beego/pkg/apperror"
	"context"
	"net/http"
	"strconv"

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
	role, ok := parseRoleParam(ctx)
	if !ok {
		writeError(ctx, apperror.BadRequest("role_invalid"))
		return
	}
	result, err := h.newInviteUC(h.ormer).Current(role)
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, mapInvitationVo(result))
}

func (h *AdminInvitationHandler) Issue(ctx *beecontext.Context) {
	staffID := staffIDFromCookie(ctx)
	if staffID == 0 {
		writeError(ctx, apperror.Unauthorized("unauthenticated"))
		return
	}
	role, ok := parseRoleParam(ctx)
	if !ok {
		writeError(ctx, apperror.BadRequest("role_invalid"))
		return
	}
	var result *dominvitation.Vo
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		var e error
		result, e = h.newInviteUC(tx).Issue(role)
		return e
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, mapInvitationVo(result))
}

func parseRoleParam(ctx *beecontext.Context) (int, bool) {
	raw := ctx.Input.Query("role")
	if raw == "" {
		return 2, true
	}
	role, err := strconv.Atoi(raw)
	if err != nil || (role != 1 && role != 2) {
		return 0, false
	}
	return role, true
}

func mapInvitationVo(v *dominvitation.Vo) map[string]interface{} {
	return map[string]interface{}{
		"found":       true,
		"url":         v.URL,
		"display_url": v.DisplayURL,
		"token":       v.Token,
	}
}
