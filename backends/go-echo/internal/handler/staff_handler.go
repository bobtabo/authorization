package handler

import (
	domstaff "authorization-go-echo/internal/domain/staff"
	ustaff "authorization-go-echo/internal/usecase/staff"
	"authorization-go-echo/pkg/apperror"
	"net/http"
	"strconv"
	"strings"

	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

type StaffHandler struct {
	db         *gorm.DB
	newStaffUC func(*gorm.DB) *ustaff.Interactor
}

func NewStaffHandler(db *gorm.DB, newStaffUC func(*gorm.DB) *ustaff.Interactor) *StaffHandler {
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
		Role int `json:"role"`
	}
	if err = c.Bind(&body); err != nil || body.Role == 0 {
		return apperror.BadRequest("validation_error")
	}

	executorID := staffIDFromCookie(c)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).UpdateRole(ustaff.UpdateRoleDto{
			ID:         id,
			Role:       body.Role,
			ExecutorID: executorID,
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
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).Restore(id)
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
	executorID := staffIDFromCookie(c)
	if txErr := h.db.Transaction(func(tx *gorm.DB) error {
		return h.newStaffUC(tx).Destroy(ustaff.DestroyDto{
			ID:         id,
			ExecutorID: executorID,
		})
	}); txErr != nil {
		return txErr
	}
	return c.JSON(http.StatusOK, map[string]interface{}{"id": id})
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
