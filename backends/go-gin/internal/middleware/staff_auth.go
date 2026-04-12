package middleware

import (
	"authorization-go/pkg/apperror"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

const StaffIDKey = "staffID"

// StaffAuth はリクエストの staff_id クッキーを検証します（任意。ハンドラー側でも個別チェック可）。
func StaffAuth() gin.HandlerFunc {
	return func(c *gin.Context) {
		v, err := c.Cookie("staff_id")
		if err != nil || v == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"message": "unauthenticated"})
			c.Abort()
			return
		}
		id, err := strconv.ParseUint(v, 10, 32)
		if err != nil || id == 0 {
			c.JSON(http.StatusUnauthorized, gin.H{"message": "unauthenticated"})
			c.Abort()
			return
		}
		c.Set(StaffIDKey, uint(id))
		c.Next()
	}
}

// ErrorHandler はハンドラーが c.Error() でセットした AppError を JSON レスポンスに変換します。
func ErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		if len(c.Errors) == 0 {
			return
		}
		err := c.Errors.Last().Err
		if appErr, ok := apperror.Is(err); ok {
			c.JSON(appErr.Code, gin.H{"message": appErr.Message})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"message": "internal_server_error"})
	}
}
