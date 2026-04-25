package handler

import (
	dominvitation "authorization-go/internal/domain/invitation"
	uinvitation "authorization-go/internal/usecase/invitation"
	"net/http"

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
// GET /api/admin/invitation
func (h *AdminInvitationHandler) Index(c *gin.Context) {
	result, err := h.newInviteUC(h.db).Current()
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, mapInvitationVo(result))
}

// Issue は新しい招待を発行して返します。
// GET /api/admin/invitation/issue
func (h *AdminInvitationHandler) Issue(c *gin.Context) {
	var result *dominvitation.Vo
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		var e error
		result, e = h.newInviteUC(tx).Issue()
		return e
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, mapInvitationVo(result))
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
