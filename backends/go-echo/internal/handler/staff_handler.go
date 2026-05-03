package handler

import (
	"authorization-go-echo/ent"
	domstaff "authorization-go-echo/internal/domain/staff"
	ustaff "authorization-go-echo/internal/usecase/staff"
	"authorization-go-echo/pkg/apperror"
	"net/http"
	"strconv"
	"strings"

	"github.com/labstack/echo/v4"
)

type StaffHandler struct {
	db         *ent.Client
	newStaffUC func(*ent.Client) *ustaff.Interactor
}

func NewStaffHandler(db *ent.Client, newStaffUC func(*ent.Client) *ustaff.Interactor) *StaffHandler {
	return &StaffHandler{db: db, newStaffUC: newStaffUC}
}

func (h *StaffHandler) Index(c echo.Context) error {
	cond := domstaff.Condition{}
	if kw := c.QueryParam("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	cond.Roles = parseIntList(c.QueryParams()["roles"])
	staffs, err := h.newStaffUC(h.db).FindByCondition(cond)
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"items": mapStaffList(staffs)})
}

func (h *StaffHandler) UpdateRole(c echo.Context) error {
	id, err := parseUintParam(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	var body struct {
		Role    int `json:"role"`
		Version int `json:"version"`
	}
	if err = c.Bind(&body); err != nil || body.Role == 0 {
		return apperror.BadRequest("validation_error")
	}
	executorID := staffIDFromCookie(c)
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		return h.newStaffUC(tx.Client()).UpdateRole(ustaff.UpdateRoleDto{
			ID: id, Role: body.Role, ExecutorID: executorID, Version: body.Version,
		})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"id": id})
}

func (h *StaffHandler) Restore(c echo.Context) error {
	id, err := parseUintParam(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	var body struct {
		Version int `json:"version"`
	}
	if err = c.Bind(&body); err != nil {
		return apperror.BadRequest("validation_error")
	}
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		return h.newStaffUC(tx.Client()).Restore(ustaff.RestoreDto{ID: id, Version: body.Version})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"id": id})
}

func (h *StaffHandler) Destroy(c echo.Context) error {
	id, err := parseUintParam(c, "id")
	if err != nil {
		return apperror.BadRequest("invalid_id")
	}
	var body struct {
		Version int `json:"version"`
	}
	if err = c.Bind(&body); err != nil {
		return apperror.BadRequest("validation_error")
	}
	executorID := staffIDFromCookie(c)
	if txErr := withTx(c.Request().Context(), h.db, func(tx *ent.Tx) error {
		return h.newStaffUC(tx.Client()).Destroy(ustaff.DestroyDto{ID: id, ExecutorID: executorID, Version: body.Version})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"id": id})
}

func mapStaffList(staffs []*domstaff.ListItem) []map[string]interface{} {
	out := make([]map[string]interface{}, 0, len(staffs))
	for _, s := range staffs {
		out = append(out, map[string]interface{}{
			"id": s.ID, "name": s.Name, "email": s.Email, "role": s.Role,
			"status": s.Status, "created_at": formatTime(s.CreatedAt), "updated_at": formatTime(s.UpdatedAt),
		})
	}
	return out
}

func parseUintParam(c echo.Context, key string) (uint, error) {
	v, err := strconv.ParseUint(c.Param(key), 10, 32)
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
