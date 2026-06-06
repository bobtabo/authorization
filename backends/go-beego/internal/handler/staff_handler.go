package handler

import (
	domstaff "authorization-go-beego/internal/domain/staff"
	"authorization-go-beego/internal/infrastructure/persistence"
	ustaff "authorization-go-beego/internal/usecase/staff"
	"authorization-go-beego/pkg/apperror"
	"context"
	"encoding/json"
	"net/http"
	"strconv"
	"strings"

	beecontext "github.com/beego/beego/v2/server/web/context"
	"github.com/beego/beego/v2/client/orm"
)

type StaffHandler struct {
	ormer      orm.Ormer
	newStaffUC func(persistence.QueryOrmer) *ustaff.Interactor
}

func NewStaffHandler(ormer orm.Ormer, newStaffUC func(persistence.QueryOrmer) *ustaff.Interactor) *StaffHandler {
	return &StaffHandler{ormer: ormer, newStaffUC: newStaffUC}
}

func (h *StaffHandler) Index(ctx *beecontext.Context) {
	cond := domstaff.Condition{}

	if kw := ctx.Input.Query("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	cond.Roles = parseIntList(ctx.Request.URL.Query()["roles"])

	limit := 20
	if v := ctx.Input.Query("limit"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			limit = n
		}
	}
	page := 1
	if v := ctx.Input.Query("page"); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			page = n
		}
	}
	offset := limit * (page - 1)
	cond.Offset = offset
	cond.Limit = limit
	cond.Sort = ctx.Input.Query("sort")
	cond.SortType = ctx.Input.Query("sort_type")

	uc := h.newStaffUC(h.ormer)
	count, err := uc.CountByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}
	staffs, err := uc.FindByCondition(cond)
	if err != nil {
		writeError(ctx, err)
		return
	}

	pager := BuildPager(count, limit, offset, len(staffs))
	writeJSON(ctx, http.StatusOK, map[string]interface{}{
		"data":  mapStaffList(staffs),
		"pager": pager,
	})
}

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
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
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

func (h *StaffHandler) Restore(ctx *beecontext.Context) {
	id, err := parseUintParam(ctx, ":id")
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
		return h.newStaffUC(tx).Restore(ustaff.RestoreDto{ID: id})
	}); txErr != nil {
		writeError(ctx, txErr)
		return
	}
	writeJSON(ctx, http.StatusOK, map[string]interface{}{"id": id})
}

func (h *StaffHandler) Destroy(ctx *beecontext.Context) {
	id, err := parseUintParam(ctx, ":id")
	if err != nil {
		writeError(ctx, apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(ctx)
	if txErr := h.ormer.DoTx(func(_ context.Context, tx orm.TxOrmer) error {
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

func parseUintParam(ctx *beecontext.Context, key string) (uint, error) {
	v, err := strconv.ParseUint(ctx.Input.Param(key), 10, 32)
	return uint(v), err
}

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
