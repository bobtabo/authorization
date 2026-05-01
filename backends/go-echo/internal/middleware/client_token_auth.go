package middleware

import (
	uclient "authorization-go-echo/internal/usecase/client"
	"authorization-go-echo/pkg/apperror"
	"net/http"
	"strings"

	"github.com/labstack/echo/v4"
)

func ClientTokenAuth(clientUC *uclient.Interactor) echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			auth := c.Request().Header.Get("Authorization")
			token := ""
			if strings.HasPrefix(auth, "Bearer ") {
				token = strings.TrimPrefix(auth, "Bearer ")
			}
			if token == "" {
				return c.JSON(http.StatusUnauthorized, map[string]string{"message": "client_not_found"})
			}

			ok, err := clientUC.AuthenticateByToken(token)
			if err != nil {
				return c.JSON(http.StatusInternalServerError, map[string]string{"message": "internal_server_error"})
			}
			if !ok {
				appErr := apperror.Unauthorized("client_not_found")
				return c.JSON(appErr.Code, map[string]string{"message": appErr.Message})
			}

			c.Set("bearerToken", token)
			return next(c)
		}
	}
}
