package handler

import (
	"authorization-go-echo/pkg/apperror"
	"regexp"

	"github.com/go-playground/validator/v10"
)

var validate = validator.New()

var telRegex = regexp.MustCompile(`^\d{10,11}$`)

func init() {
	_ = validate.RegisterValidation("tel", func(fl validator.FieldLevel) bool {
		return telRegex.MatchString(fl.Field().String())
	})
}

// StoreClientBody はクライアント登録リクエストボディです。
type StoreClientBody struct {
	Name     string `json:"name"      validate:"required,max=255"`
	PostCode string `json:"post_code" validate:"required,max=8"`
	Pref     string `json:"pref"      validate:"required,max=50"`
	City     string `json:"city"      validate:"required,max=100"`
	Address  string `json:"address"   validate:"required,max=255"`
	Building string `json:"building"  validate:"omitempty,max=255"`
	Tel      string `json:"tel"       validate:"required,tel"`
	Email    string `json:"email"     validate:"required,email,max=255"`
}

// UpdateClientBody はクライアント更新リクエストボディです。
type UpdateClientBody struct {
	Name     *string `json:"name"      validate:"omitempty,max=255"`
	PostCode *string `json:"post_code" validate:"omitempty,max=8"`
	Pref     *string `json:"pref"      validate:"omitempty,max=50"`
	City     *string `json:"city"      validate:"omitempty,max=100"`
	Address  *string `json:"address"   validate:"omitempty,max=255"`
	Building *string `json:"building"  validate:"omitempty,max=255"`
	Tel      *string `json:"tel"       validate:"omitempty,tel"`
	Email    *string `json:"email"     validate:"omitempty,email,max=255"`
	Status   *int    `json:"status"`
	Version  int     `json:"version"`
}

func validateStruct(s interface{}) error {
	if err := validate.Struct(s); err != nil {
		return apperror.UnprocessableEntity("validation_error")
	}
	return nil
}
