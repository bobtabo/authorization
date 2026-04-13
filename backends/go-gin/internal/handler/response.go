package handler

import (
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

const timeFormat = "2006-01-02 15:04"

// staffIDFromCookie は staff_id クッキーから uint を取得します。0 は未認証を示します。
func staffIDFromCookie(c *gin.Context) uint {
	v, err := c.Cookie("staff_id")
	if err != nil || v == "" {
		return 0
	}
	id, err := strconv.ParseUint(v, 10, 32)
	if err != nil {
		return 0
	}
	return uint(id)
}

// formatTime は time.Time を "2006-01-02 15:04" 形式の文字列に変換します。
func formatTime(t time.Time) string {
	return t.Format(timeFormat)
}

// formatTimePtr は *time.Time を文字列ポインタに変換します（nil はそのまま nil）。
func formatTimePtr(t *time.Time) *string {
	if t == nil {
		return nil
	}
	s := t.Format(timeFormat)
	return &s
}
