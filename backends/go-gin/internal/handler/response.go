package handler

import (
	"time"

	"github.com/gin-gonic/gin"
)

const timeFormat = "2006-01-02 15:04"

// staffIDFromCookie は署名済み staff_id クッキーを検証して uint を取得します。
// クッキーが無い・形式不正・署名不正（改ざん）の場合は 0（未認証）を返します。
func staffIDFromCookie(c *gin.Context, secret string) uint {
	v, err := c.Cookie("staff_id")
	if err != nil || v == "" {
		return 0
	}
	return verifyStaffID(v, secret)
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
