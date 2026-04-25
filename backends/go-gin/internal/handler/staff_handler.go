package handler

import (
	domstaff "authorization-go/internal/domain/staff"
	ustaff "authorization-go/internal/usecase/staff"
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// StaffHandler はスタッフ関連のHTTPハンドラーを提供します。
type StaffHandler struct {
	db         *gorm.DB
	newStaffUC func(*gorm.DB) *ustaff.Interactor
}

// NewStaffHandler は StaffHandler を生成します。
//
// db: GORM DB インスタンス
// newStaffUC: スタッフユースケースファクトリ
func NewStaffHandler(db *gorm.DB, newStaffUC func(*gorm.DB) *ustaff.Interactor) *StaffHandler {
	return &StaffHandler{db: db, newStaffUC: newStaffUC}
}

// Index は検索条件に合致するスタッフ一覧を返します。
// GET /api/staffs
func (h *StaffHandler) Index(c *gin.Context) {
	cond := domstaff.Condition{}

	if kw := c.Query("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	cond.Roles = parseIntList(c.QueryArray("roles"))

	staffs, err := h.newStaffUC(h.db).FindByCondition(cond)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"items": mapStaffList(staffs)})
}

// UpdateRole はスタッフの権限を更新します。
// PATCH /api/staffs/:id/updateRole
func (h *StaffHandler) UpdateRole(c *gin.Context) {
	id, err := parseUintParam(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}

	var body struct {
		Role int `json:"role" binding:"required"`
	}
	if err = c.ShouldBindJSON(&body); err != nil {
		_ = c.Error(apperror.BadRequest("validation_error"))
		return
	}

	executorID := staffIDFromCookie(c)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).UpdateRole(ustaff.UpdateRoleDto{
			ID:         id,
			Role:       body.Role,
			ExecutorID: executorID,
		})
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// Restore はスタッフの論理削除を復元します。
// PATCH /api/staffs/:id/restore
func (h *StaffHandler) Restore(c *gin.Context) {
	id, err := parseUintParam(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).Restore(id)
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// Destroy はスタッフを論理削除します。
// DELETE /api/staffs/:id/delete
func (h *StaffHandler) Destroy(c *gin.Context) {
	id, err := parseUintParam(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(c)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).Destroy(ustaff.DestroyDto{
			ID:         id,
			ExecutorID: executorID,
		})
	}); txErr != nil {
		_ = c.Error(txErr)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// ---------- 変換ヘルパー ----------

// mapStaffList はスタッフ一覧 Vo をレスポンス用マップのスライスに変換します。
func mapStaffList(staffs []*domstaff.ListItem) []gin.H {
	out := make([]gin.H, 0, len(staffs))
	for _, s := range staffs {
		out = append(out, gin.H{
			"id":         s.ID,
			"name":       s.Name,
			"email":      s.Email,
			"role":       s.Role,
			"status":     s.Status,
			"created_at": formatTime(s.CreatedAt),
			"updated_at": formatTime(s.UpdatedAt),
		})
	}
	return out
}

// parseUintParam はパスパラメータを uint に変換します。
func parseUintParam(c *gin.Context, key string) (uint, error) {
	v, err := strconv.ParseUint(c.Param(key), 10, 32)
	return uint(v), err
}

// parseIntList はクエリパラメータの文字列スライスをカンマ区切りで展開し int スライスに変換します。
func parseIntList(raw []string) []int {
	var out []int
	for _, v := range raw {
		for _, s := range strings.Split(v, ",") {
			s = strings.TrimSpace(s)
			if s == "" {
				continue
			}
			if n, err := strconv.Atoi(s); err == nil {
				out = append(out, n)
			}
		}
	}
	return out
}
