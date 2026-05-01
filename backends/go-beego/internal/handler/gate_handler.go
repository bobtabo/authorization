package handler

import (
	ugate "authorization-go-beego/internal/usecase/gate"
	"authorization-go-beego/pkg/apperror"
	"net/http"
	"strings"

	beecontext "github.com/beego/beego/v2/server/web/context"
)

type GateHandler struct {
	gateUC *ugate.Interactor
}

func NewGateHandler(gateUC *ugate.Interactor) *GateHandler {
	return &GateHandler{gateUC: gateUC}
}

func (h *GateHandler) Issue(ctx *beecontext.Context) {
	member := ctx.Input.Query("member")
	if member == "" {
		writeError(ctx, apperror.BadRequest("member_required"))
		return
	}

	auth := ctx.Input.Header("Authorization")
	accessToken := strings.TrimPrefix(auth, "Bearer ")

	vo, err := h.gateUC.IssueToken(ugate.IssueDto{
		AccessToken: accessToken,
		MemberID:    member,
	})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"token": vo.Token})
}

func (h *GateHandler) Verify(ctx *beecontext.Context) {
	identifier := ctx.Input.Param(":identifier")
	token := ctx.Input.Query("token")
	if token == "" {
		writeError(ctx, apperror.BadRequest("token_required"))
		return
	}

	vo, err := h.gateUC.Verify(ugate.VerifyDto{
		Identifier: identifier,
		Token:      token,
	})
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, vo.Claims)
}
