// Package apperror はHTTPステータスコードを持つアプリケーション例外を提供します。
package apperror

import (
	"errors"
	"net/http"
)

// AppError はHTTPステータスコードとメッセージキーを持つアプリケーション例外です。
type AppError struct {
	Code    int
	Message string
}

// Error は error インターフェースを実装します。
func (e *AppError) Error() string { return e.Message }

// BadRequest は 400 Bad Request の AppError を生成します。
func BadRequest(message string) *AppError {
	return &AppError{Code: http.StatusBadRequest, Message: message}
}

// Unauthorized は 401 Unauthorized の AppError を生成します。
func Unauthorized(message string) *AppError {
	return &AppError{Code: http.StatusUnauthorized, Message: message}
}

// Forbidden は 403 Forbidden の AppError を生成します。
func Forbidden(message string) *AppError {
	return &AppError{Code: http.StatusForbidden, Message: message}
}

// NotFound は 404 Not Found の AppError を生成します。
func NotFound(message string) *AppError {
	return &AppError{Code: http.StatusNotFound, Message: message}
}

// Conflict は 409 Conflict の AppError を生成します。
func Conflict(message string) *AppError {
	return &AppError{Code: http.StatusConflict, Message: message}
}

// Internal は 500 Internal Server Error の AppError を生成します。
func Internal(message string) *AppError {
	return &AppError{Code: http.StatusInternalServerError, Message: message}
}

// Is は errors.As によるアンラップをサポートします。
func Is(err error) (*AppError, bool) {
	var appErr *AppError
	if errors.As(err, &appErr) {
		return appErr, true
	}
	return nil, false
}
