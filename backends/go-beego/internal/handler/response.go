package handler

import (
	"authorization-go-beego/pkg/apperror"
	"net/http"
	"strconv"
	"time"

	beecontext "github.com/beego/beego/v2/server/web/context"
)

const timeFormat = "2006-01-02 15:04"

func staffIDFromCookie(ctx *beecontext.Context) uint {
	v := ctx.GetCookie("staff_id")
	if v == "" {
		return 0
	}
	id, err := strconv.ParseUint(v, 10, 32)
	if err != nil {
		return 0
	}
	return uint(id)
}

func setStaffCookie(ctx *beecontext.Context, staffID uint, maxAge int, secure bool) {
	ctx.SetCookie("staff_id", strconv.Itoa(int(staffID)), maxAge, "/", "", secure, true)
}

func clearStaffCookie(ctx *beecontext.Context, secure bool) {
	ctx.SetCookie("staff_id", "", -1, "/", "", secure, true)
}

func formatTime(t time.Time) string {
	return t.Format(timeFormat)
}

func formatTimePtr(t *time.Time) *string {
	if t == nil {
		return nil
	}
	s := t.Format(timeFormat)
	return &s
}

func writeJSON(ctx *beecontext.Context, status int, data interface{}) {
	ctx.Output.Status = status
	_ = ctx.Output.JSON(data, false, false)
}

func writeError(ctx *beecontext.Context, err error) {
	if appErr, ok := apperror.Is(err); ok {
		ctx.Output.Status = appErr.Code
		_ = ctx.Output.JSON(map[string]string{"message": appErr.Message}, false, false)
		return
	}
	ctx.Output.Status = http.StatusInternalServerError
	_ = ctx.Output.JSON(map[string]string{"message": "internal_server_error"}, false, false)
}
