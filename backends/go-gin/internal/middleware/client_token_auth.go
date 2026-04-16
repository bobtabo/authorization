package middleware

import (
	uclient "authorization-go/internal/usecase/client"
	"authorization-go/pkg/apperror"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

// ClientTokenAuth はBearerトークンでクライアントを認証します。
func ClientTokenAuth(clientUC *uclient.Interactor) gin.HandlerFunc {
	return func(c *gin.Context) {
		auth := c.GetHeader("Authorization")
		token := ""
		if strings.HasPrefix(auth, "Bearer ") {
			token = strings.TrimPrefix(auth, "Bearer ")
		}
		if token == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"message": "client_not_found"})
			c.Abort()
			return
		}

		ok, err := clientUC.AuthenticateByToken(token)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"message": "internal_server_error"})
			c.Abort()
			return
		}
		if !ok {
			appErr := apperror.Unauthorized("client_not_found")
			c.JSON(appErr.Code, gin.H{"message": appErr.Message})
			c.Abort()
			return
		}

		c.Set("bearerToken", token)
		c.Next()
	}
}
