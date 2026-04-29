package handler

import (
	"net/http"
	"strconv"
	"time"

	"github.com/labstack/echo/v4"
)

const timeFormat = "2006-01-02 15:04"

func staffIDFromCookie(c echo.Context) uint {
	cookie, err := c.Cookie("staff_id")
	if err != nil || cookie.Value == "" {
		return 0
	}
	id, err := strconv.ParseUint(cookie.Value, 10, 32)
	if err != nil {
		return 0
	}
	return uint(id)
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

func setStaffCookie(c echo.Context, staffID uint, maxAge int, secure bool) {
	c.SetCookie(&http.Cookie{
		Name:     "staff_id",
		Value:    strconv.Itoa(int(staffID)),
		MaxAge:   maxAge,
		Path:     "/",
		Secure:   secure,
		HttpOnly: true,
	})
}

func clearStaffCookie(c echo.Context, secure bool) {
	c.SetCookie(&http.Cookie{
		Name:     "staff_id",
		Value:    "",
		MaxAge:   -1,
		Path:     "/",
		Secure:   secure,
		HttpOnly: true,
	})
}
