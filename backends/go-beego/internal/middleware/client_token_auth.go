package middleware

import (
	uclient "authorization-go-beego/internal/usecase/client"
	"net/http"
	"strings"

	beecontext "github.com/beego/beego/v2/server/web/context"
)

func ClientTokenAuth(clientUC *uclient.Interactor) func(*beecontext.Context) {
	return func(ctx *beecontext.Context) {
		auth := ctx.Input.Header("Authorization")
		token := ""
		if strings.HasPrefix(auth, "Bearer ") {
			token = strings.TrimPrefix(auth, "Bearer ")
		}
		if token == "" {
			ctx.Output.Status = http.StatusUnauthorized
			_ = ctx.Output.JSON(map[string]string{"message": "client_not_found"}, false, false)
			return
		}

		ok, err := clientUC.AuthenticateByToken(token)
		if err != nil {
			ctx.Output.Status = http.StatusInternalServerError
			_ = ctx.Output.JSON(map[string]string{"message": "internal_server_error"}, false, false)
			return
		}
		if !ok {
			ctx.Output.Status = http.StatusUnauthorized
			_ = ctx.Output.JSON(map[string]string{"message": "client_not_found"}, false, false)
			return
		}
	}
}
