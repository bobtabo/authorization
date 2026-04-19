package handler

import (
	domstaff "authorization-go/internal/domain/staff"
	ustaff "authorization-go/internal/usecase/staff"
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
)

type StaffHandler struct {
	staffUC *ustaff.Interactor
}

func NewStaffHandler(staffUC *ustaff.Interactor) *StaffHandler {
	return &StaffHandler{staffUC: staffUC}
}

// GET /api/staffs
func (h *StaffHandler) Index(c *gin.Context) {
	cond := domstaff.Condition{}

	if kw := c.Query("keyword"); kw != "" {
		cond.Keyword = &kw
	}
	cond.Roles = parseIntList(c.QueryArray("roles"))

	staffs, err := h.staffUC.FindByCondition(cond)
	if err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"items": mapStaffList(staffs)})
}

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
	if err = h.staffUC.UpdateRole(ustaff.UpdateRoleDto{
		ID:         id,
		Role:       body.Role,
		ExecutorID: executorID,
	}); err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// PATCH /api/staffs/:id/restore
func (h *StaffHandler) Restore(c *gin.Context) {
	id, err := parseUintParam(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	if err = h.staffUC.Restore(id); err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// DELETE /api/staffs/:id/delete
func (h *StaffHandler) Destroy(c *gin.Context) {
	id, err := parseUintParam(c, "id")
	if err != nil {
		_ = c.Error(apperror.BadRequest("invalid_id"))
		return
	}
	executorID := staffIDFromCookie(c)
	if err = h.staffUC.Destroy(ustaff.DestroyDto{
		ID:         id,
		ExecutorID: executorID,
	}); err != nil {
		_ = c.Error(err)
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

// ---------- 変換ヘルパー ----------

func mapStaffList(staffs []*domstaff.Staff) []gin.H {
	out := make([]gin.H, 0, len(staffs))
	for _, s := range staffs {
		out = append(out, gin.H{
			"id":         s.ID,
			"name":       s.Name,
			"email":      s.Email,
			"role":       s.Role,
			"status":     ustaff.Status(s),
			"created_at": formatTime(s.CreatedAt),
			"updated_at": formatTime(s.UpdatedAt),
		})
	}
	return out
}

func parseUintParam(c *gin.Context, key string) (uint, error) {
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
