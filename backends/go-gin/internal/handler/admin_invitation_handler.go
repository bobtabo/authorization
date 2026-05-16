package handler

import (
	dominvitation "authorization-go/internal/domain/invitation"
	uinvitation "authorization-go/internal/usecase/invitation"
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// AdminInvitationHandler は管理者招待関連のHTTPハンドラーを提供します。
type AdminInvitationHandler struct {
	db          *gorm.DB
	newInviteUC func(*gorm.DB) *uinvitation.Interactor
}

// NewAdminInvitationHandler は AdminInvitationHandler を生成します。
//
// db: GORM DB インスタンス
// newInviteUC: 招待ユースケースファクトリ
func NewAdminInvitationHandler(db *gorm.DB, newInviteUC func(*gorm.DB) *uinvitation.Interactor) *AdminInvitationHandler {
	return &AdminInvitationHandler{db: db, newInviteUC: newInviteUC}
}

// Index は現在の招待情報を返します。
// GET /api/admin/invitation?role=2
func (h *AdminInvitationHandler) Index(c *gin.Context) {
	role, ok := parseRoleParam(c)
	if !ok {
		return
	}
	result, err := h.newInviteUC(h.db).Current(uinvitation.CurrentDto{Role: role})
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapInvitationVo(result))
}

// Issue は新しい招待を発行して返します。
// GET /api/admin/invitation/issue?role=2
func (h *AdminInvitationHandler) Issue(c *gin.Context) {
	role, ok := parseRoleParam(c)
	if !ok {
		return
	}
	var result *dominvitation.Vo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		result, e = h.newInviteUC(tx).Issue(uinvitation.IssueDto{Role: role})
		return e
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, mapInvitationVo(result))
}

// ---------- プライベートヘルパー ----------

// parseRoleParam は role クエリパラメータを検証して返します。
// 省略時はデフォルト 2、1 か 2 以外は 400 を返します。
func parseRoleParam(c *gin.Context) (int, bool) {
	roleStr := c.DefaultQuery("role", "2")
	role, err := strconv.Atoi(roleStr)
	if err != nil || (role != 1 && role != 2) {
		_ = c.Error(apperror.BadRequest("invalid_role"))
		return 0, false
	}
	return role, true
}

// ---------- 変換ヘルパー ----------

// mapInvitationVo は招待 Vo をレスポンス用マップに変換します。
func mapInvitationVo(v *dominvitation.Vo) gin.H {
	return gin.H{
		"found":       true,
		"url":         v.URL,
		"display_url": v.DisplayURL,
		"token":       v.Token,
	}
}
