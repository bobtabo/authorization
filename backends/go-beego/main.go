package main

import (
	"authorization-go-beego/internal/config"
	"authorization-go-beego/internal/handler"
	"authorization-go-beego/internal/infrastructure/cache"
	"authorization-go-beego/internal/infrastructure/db"
	"authorization-go-beego/internal/infrastructure/mail"
	"authorization-go-beego/internal/infrastructure/persistence"
	"authorization-go-beego/internal/middleware"
	uauth "authorization-go-beego/internal/usecase/auth"
	uclient "authorization-go-beego/internal/usecase/client"
	ugate "authorization-go-beego/internal/usecase/gate"
	uinvitation "authorization-go-beego/internal/usecase/invitation"
	unotification "authorization-go-beego/internal/usecase/notification"
	ustaff "authorization-go-beego/internal/usecase/staff"
	"log"
	"net/http"

	"github.com/beego/beego/v2/server/web"
	beecontext "github.com/beego/beego/v2/server/web/context"
)

func main() {
	cfg := config.Load()

	ormer, err := db.New(cfg)
	if err != nil {
		log.Fatalf("db connect: %v", err)
	}

	rdb := cache.New(cfg)

	gateCacheRepo := cache.NewRedisGateRepository(rdb, cfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, cfg)

	newAuthUC := func(o persistence.QueryOrmer) *uauth.Interactor {
		return uauth.NewInteractor(persistence.NewOrmStaffRepository(o), invitationAuthRepo)
	}
	newClientUC := func(o persistence.QueryOrmer) *uclient.Interactor {
		return uclient.NewInteractor(persistence.NewOrmClientRepository(o))
	}
	newStaffUC := func(o persistence.QueryOrmer) *ustaff.Interactor {
		return ustaff.NewInteractor(persistence.NewOrmStaffRepository(o))
	}
	newInviteUC := func(o persistence.QueryOrmer) *uinvitation.Interactor {
		return uinvitation.NewInteractor(persistence.NewOrmInvitationRepository(o, cfg.App.FrontendURL), invitationAuthRepo)
	}
	newNotifUC := func(o persistence.QueryOrmer) *unotification.Interactor {
		return unotification.NewInteractor(
			persistence.NewOrmNotificationRepository(o),
			persistence.NewOrmStaffRepository(o),
		)
	}

	gateUC := ugate.NewInteractor(persistence.NewOrmClientRepository(ormer), gateCacheRepo, cfg)

	mailer := mail.NewMailer(cfg.Mail)

	authH := handler.NewAuthHandler(ormer, newAuthUC, newInviteUC, cfg)
	clientH := handler.NewClientHandler(ormer, newClientUC, newNotifUC, mailer)
	staffH := handler.NewStaffHandler(ormer, newStaffUC)
	adminInvitationH := handler.NewAdminInvitationHandler(ormer, newInviteUC)
	gateH := handler.NewGateHandler(gateUC)
	notificationH := handler.NewNotificationHandler(ormer, newNotifUC, cfg)

	web.BConfig.CopyRequestBody = true
	web.BConfig.WebConfig.AutoRender = false
	web.BConfig.Log.AccessLogs = false

	// CORSフィルター（全ルートに適用）
	web.InsertFilter("*", web.BeforeRouter, func(ctx *beecontext.Context) {
		ctx.Output.Header("Access-Control-Allow-Origin", cfg.App.FrontendURL)
		ctx.Output.Header("Access-Control-Allow-Credentials", "true")
		ctx.Output.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")
		ctx.Output.Header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		if ctx.Input.Method() == "OPTIONS" {
			ctx.Output.Status = http.StatusNoContent
			_ = ctx.Output.Body([]byte{})
		}
	}, web.WithReturnOnOutput(true))

	// Gate APIのクライアント認証フィルター
	web.InsertFilter("/api/gate/issue", web.BeforeExec,
		middleware.ClientTokenAuth(newClientUC(ormer)),
		web.WithReturnOnOutput(true),
	)

	web.Get("/auth/google/redirect", authH.GoogleRedirect)
	web.Get("/auth/google/callback", authH.GoogleCallback)
	web.Get("/auth/github/redirect", authH.GithubRedirect)
	web.Get("/auth/github/callback", authH.GithubCallback)

	web.Get("/api/auth/me", authH.GetMyProfile)
	web.Get("/api/auth/login", authH.Login)
	web.Get("/api/auth/logout", authH.Logout)
	web.Get("/api/auth/invitation/:token", authH.Invitation)

	web.Get("/api/clients", clientH.Index)
	web.Post("/api/clients/store", clientH.Store)
	web.Put("/api/clients/:id/update", clientH.Update)
	web.Get("/api/clients/:id", clientH.Show)
	web.Delete("/api/clients/:id/delete", clientH.Destroy)

	web.Get("/api/clients/:identifier/qr", clientH.GetQr)
	web.Get("/api/clients/:identifier/info", clientH.GetInfo)
	web.Patch("/api/clients/:identifier/start", clientH.Start)
	web.Patch("/api/clients/:identifier/stop", clientH.Stop)

	web.Get("/api/staffs", staffH.Index)
	web.Patch("/api/staffs/:id/updateRole", staffH.UpdateRole)
	web.Patch("/api/staffs/:id/restore", staffH.Restore)
	web.Delete("/api/staffs/:id/delete", staffH.Destroy)

	web.Get("/api/admin/invitation", adminInvitationH.Index)
	web.Get("/api/admin/invitation/issue", adminInvitationH.Issue)

	web.Get("/api/gate/issue", gateH.Issue)
	web.Get("/api/gate/client/:identifier/verify", gateH.Verify)

	web.Get("/api/notifications/counts", notificationH.Counts)
	web.Get("/api/notifications", notificationH.Index)
	web.Patch("/api/notifications", notificationH.ReadAll)
	web.Patch("/api/notifications/:id", notificationH.Read)

	log.Printf("starting go-beego server on :%s", cfg.App.Port)
	web.Run(":" + cfg.App.Port)
}
