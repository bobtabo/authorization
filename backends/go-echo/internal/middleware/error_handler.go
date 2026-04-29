package middleware

import (
	"authorization-go-echo/pkg/apperror"
	"net/http"

	"github.com/labstack/echo/v4"
)

func ErrorHandler() echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			err := next(c)
			if err == nil {
				return nil
			}
			if appErr, ok := apperror.Is(err); ok {
				return c.JSON(appErr.Code, map[string]string{"message": appErr.Message})
			}
			if he, ok := err.(*echo.HTTPError); ok {
				msg, _ := he.Message.(string)
				return c.JSON(he.Code, map[string]string{"message": msg})
			}
			return c.JSON(http.StatusInternalServerError, map[string]string{"message": "internal_server_error"})
		}
	}
}
