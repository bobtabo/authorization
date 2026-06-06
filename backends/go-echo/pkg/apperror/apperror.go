package apperror

import (
	"errors"
	"net/http"
)

type AppError struct {
	Code    int
	Message string
}

func (e *AppError) Error() string { return e.Message }

func BadRequest(message string) *AppError {
	return &AppError{Code: http.StatusBadRequest, Message: message}
}

func Unauthorized(message string) *AppError {
	return &AppError{Code: http.StatusUnauthorized, Message: message}
}

func Forbidden(message string) *AppError {
	return &AppError{Code: http.StatusForbidden, Message: message}
}

func NotFound(message string) *AppError {
	return &AppError{Code: http.StatusNotFound, Message: message}
}

func Internal(message string) *AppError {
	return &AppError{Code: http.StatusInternalServerError, Message: message}
}

func UnprocessableEntity(message string) *AppError {
	return &AppError{Code: http.StatusUnprocessableEntity, Message: message}
}

func Conflict(message string) *AppError {
	return &AppError{Code: http.StatusConflict, Message: message}
}

func Is(err error) (*AppError, bool) {
	var appErr *AppError
	if errors.As(err, &appErr) {
		return appErr, true
	}
	return nil, false
}
