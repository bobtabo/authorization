package handler

import (
	ugate "authorization-go/internal/usecase/gate"
	"authorization-go/pkg/apperror"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

// GateHandler はゲート関連のHTTPハンドラーを提供します。
type GateHandler struct {
	gateUC *ugate.Interactor
}

// NewGateHandler は GateHandler を生成します。
//
// gateUC: ゲートユースケース
func NewGateHandler(gateUC *ugate.Interactor) *GateHandler {
	return &GateHandler{gateUC: gateUC}
}

// Issue はクライアント会員向け JWT を発行します。
// GET /api/gate/issue   (client.token ミドルウェア適用済み)
func (h *GateHandler) Issue(c *gin.Context) {
	member := c.Query("member")
	if member == "" {
		_ = c.Error(apperror.BadRequest("member_required"))
		return
	}

	auth := c.GetHeader("Authorization")
	accessToken := strings.TrimPrefix(auth, "Bearer ")

	vo, err := h.gateUC.IssueToken(ugate.IssueDto{
		AccessToken: accessToken,
		MemberID:    member,
	})
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"token": vo.Token})
}

// Verify は JWT を検証してペイロードを返します。
// GET /api/gate/client/:identifier/verify
func (h *GateHandler) Verify(c *gin.Context) {
	identifier := c.Param("identifier")
	token := c.Query("token")
	if token == "" {
		_ = c.Error(apperror.BadRequest("token_required"))
		return
	}

	vo, err := h.gateUC.Verify(ugate.VerifyDto{
		Identifier: identifier,
		Token:      token,
	})
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, vo.Claims)
}
