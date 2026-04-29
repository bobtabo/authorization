// Package handler はHTTPハンドラを提供します。
package handler

import (
	domstaff "authorization-go-beego/internal/domain/staff"
	ustaff "authorization-go-beego/internal/usecase/staff"
	"authorization-go-beego/pkg/apperror"
	"encoding/json"
	"net/http"
	"strconv"
	"strings"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"gorm.io/gorm"
)

// StaffHandler はスタッフ関連のHTTPハンドラです。
type StaffHandler struct {
	db         *gorm.DB
	newStaffUC func(*gorm.DB) *ustaff.Interactor
}

// NewStaffHandler は StaffHandler を生成します。
func NewStaffHandler(db *gorm.DB, newStaffUC func(*gorm.DB) *ustaff.Interactor) *StaffHandler {
	return &StaffHandler{db: db, newStaffUC: newStaffUC}
}

// Index はスタッフ一覧を返します。
func (h *StaffHandler) Index(ctx *beecontext.Context) {
	cond := domstaff.Condition{}

	if kw := ctx.Input.Query("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	cond.Roles = parseIntList(ctx.Request.URL.Query()["roles"])

	staffs, err := h.newStaffUC(h.db).FindByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"items": mapStaffList(staffs)})
}

// UpdateRole はスタッフのロールを変更します。
func (h *StaffHandler) UpdateRole(ctx *beecontext.Context) {
	id, err := parseUintParam(ctx, ":id")
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}

	var body struct {
		Role int `json:"role"`
	}
	if err = json.Unmarshal(ctx.Input.RequestBody, &body); err != nil || body.Role == 0 {
		writeError(ctx, apperror.BadRequest("validation_error"))
		return
	}

	executorID := staffIDFromCookie(ctx)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).UpdateRole(ustaff.UpdateRoleDto{
			ID:         id,
			Role:       body.Role,
			ExecutorID: executorID,
		})
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"id": id})
}

// Restore は論理削除したスタッフを復元します。
func (h *StaffHandler) Restore(ctx *beecontext.Context) {
	id, err := parseUintParam(ctx, ":id")
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).Restore(ustaff.RestoreDto{ID: id})
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"id": id})
}

// Destroy はスタッフを論理削除します。
func (h *StaffHandler) Destroy(ctx *beecontext.Context) {
	id, err := parseUintParam(ctx, ":id")
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(ctx)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).Destroy(ustaff.DestroyDto{
			ID:         id,
			ExecutorID: executorID,
		})
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"id": id})
}

// mapStaffList はスタッフ一覧をレスポンス用マップに変換します。
func mapStaffList(staffs []*domstaff.ListItem) []map[string]interface{} {
	out := make([]map[string]interface{}, 0, len(staffs))
	for _, s := range staffs {
		out = append(out, map[string]interface{}{
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
func parseUintParam(ctx *beecontext.Context, key string) (uint, error) {
	v, err := strconv.ParseUint(ctx.Input.Param(key), 10, 32)
	return uint(v), err
}

// parseIntList はクエリパラメータの文字列リストを int スライスに変換します。
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
