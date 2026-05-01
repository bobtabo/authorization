package handler

import (
	ugate "authorization-go-echo/internal/usecase/gate"
	"authorization-go-echo/pkg/apperror"
	"net/http"
	"strings"

	"github.com/labstack/echo/v4"
)

type GateHandler struct {
	gateUC *ugate.Interactor
}

func NewGateHandler(gateUC *ugate.Interactor) *GateHandler {
	return &GateHandler{gateUC: gateUC}
}

func (h *GateHandler) Issue(c echo.Context) error {
	member := c.QueryParam("member")
	if member == "" {
		return apperror.BadRequest("member_required")
	}

	auth := c.Request().Header.Get("Authorization")
	accessToken := strings.TrimPrefix(auth, "Bearer ")

	vo, err := h.gateUC.IssueToken(ugate.IssueDto{
		AccessToken: accessToken,
		MemberID:    member,
	})
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"token": vo.Token})
}

func (h *GateHandler) Verify(c echo.Context) error {
	identifier := c.Param("identifier")
	token := c.QueryParam("token")
	if token == "" {
		return apperror.BadRequest("token_required")
	}

	vo, err := h.gateUC.Verify(ugate.VerifyDto{
		Identifier: identifier,
		Token:      token,
	})
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, vo.Claims)
}
