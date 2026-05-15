// Package main は go-gin バックエンドのエントリポイントです。
package main

import (
	"authorization-go/internal/config"
	"authorization-go/internal/handler"
	"authorization-go/internal/infrastructure/cache"
	"authorization-go/internal/infrastructure/db"
	"authorization-go/internal/infrastructure/mail"
	"authorization-go/internal/infrastructure/persistence"
	"authorization-go/internal/middleware"
	uauth "authorization-go/internal/usecase/auth"
	uclient "authorization-go/internal/usecase/client"
	ugate "authorization-go/internal/usecase/gate"
	uinvitation "authorization-go/internal/usecase/invitation"
	unotification "authorization-go/internal/usecase/notification"
	ustaff "authorization-go/internal/usecase/staff"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func main() {
	cfg := config.Load()

	// --- DB ---
	database, err := db.New(cfg)
	if err != nil {
		log.Fatalf("db connect: %v", err)
	}

	// --- Redis ---
	rdb := cache.New(cfg)

	// --- Infrastructure: 読み取り専用リポジトリ（トランザクション不要）---
	gateCacheRepo      := cache.NewRedisGateRepository(rdb, cfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, cfg)

	// --- ユースケースファクトリ（書き込み系ハンドラーへ注入）---
	newAuthUC := func(tx *gorm.DB) *uauth.Interactor {
		return uauth.NewInteractor(persistence.NewGormStaffRepository(tx), invitationAuthRepo)
	}
	newClientUC := func(tx *gorm.DB) *uclient.Interactor {
		return uclient.NewInteractor(persistence.NewGormClientRepository(tx))
	}
	newStaffUC := func(tx *gorm.DB) *ustaff.Interactor {
		return ustaff.NewInteractor(persistence.NewGormStaffRepository(tx))
	}
	newInviteUC := func(tx *gorm.DB) *uinvitation.Interactor {
		return uinvitation.NewInteractor(persistence.NewGormInvitationRepository(tx, cfg.App.FrontendURL), invitationAuthRepo)
	}
	newNotifUC := func(tx *gorm.DB) *unotification.Interactor {
		return unotification.NewInteractor(
			persistence.NewGormNotificationRepository(tx),
			persistence.NewGormStaffRepository(tx),
		)
	}

	// --- Gate ユースケース（キャッシュのみ、GORM トランザクション不要）---
	gateUC := ugate.NewInteractor(persistence.NewGormClientRepository(database), gateCacheRepo, cfg)

	// --- Mail ---
	mailer := mail.NewMailer(cfg.Mail)

	// --- Handlers ---
	authH := handler.NewAuthHandler(database, newAuthUC, newInviteUC, cfg)
	clientH := handler.NewClientHandler(database, newClientUC, newNotifUC, mailer)
	staffH := handler.NewStaffHandler(database, newStaffUC)
	adminInvitationH := handler.NewAdminInvitationHandler(database, newInviteUC)
	gateH := handler.NewGateHandler(gateUC)
	notificationH := handler.NewNotificationHandler(database, newNotifUC, cfg)

	// --- Router ---
	if cfg.App.Env == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	r := gin.New()
	r.Use(gin.Logger(), gin.Recovery())
	r.Use(middleware.ErrorHandler())

	// CORS（フロントエンドからの Ajax リクエスト対応）
	r.Use(func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", cfg.App.FrontendURL)
		c.Header("Access-Control-Allow-Credentials", "true")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		if c.Request.Method == http.MethodOptions {
			c.AbortWithStatus(http.StatusNoContent)
			return
		}
		c.Next()
	})

	// OAuth はブラウザリダイレクトのため /api 外に配置（PHP と同じパス構造）
	r.GET("/auth/google/redirect", authH.GoogleRedirect)
	r.GET("/auth/google/callback", authH.GoogleCallback)
	r.GET("/auth/github/redirect", authH.GithubRedirect)
	r.GET("/auth/github/callback", authH.GithubCallback)

	api := r.Group("/api")
	{
		// --- auth ---
		api.GET("/auth/me", authH.GetMyProfile)
		api.GET("/auth/login", authH.Login)
		api.GET("/auth/logout", authH.Logout)
		api.GET("/auth/invitation/:token", authH.Invitation)

		// --- clients ---
		api.GET("/clients", clientH.Index)
		api.POST("/clients/store", clientH.Store)
		api.PUT("/clients/:id/update", clientH.Update)
		api.GET("/clients/:id", clientH.Show)
		api.DELETE("/clients/:id/delete", clientH.Destroy)

		// --- clients (スマホアプリ連携) ---
		api.GET("/clients/:id/qr", clientH.Qr)
		api.GET("/clients/:id/info", clientH.Info)
		api.PATCH("/clients/:id/start", clientH.Start)
		api.PATCH("/clients/:id/stop", clientH.Stop)

		// --- staffs ---
		api.GET("/staffs", staffH.Index)
		api.PATCH("/staffs/:id/updateRole", staffH.UpdateRole)
		api.PATCH("/staffs/:id/restore", staffH.Restore)
		api.DELETE("/staffs/:id/delete", staffH.Destroy)

		// --- admin ---
		api.GET("/admin/invitation", adminInvitationH.Index)
		api.GET("/admin/invitation/issue", adminInvitationH.Issue)

		// --- gate ---
		api.GET("/gate/issue",
			middleware.ClientTokenAuth(newClientUC(database)),
			gateH.Issue,
		)
		api.GET("/gate/client/:identifier/verify", gateH.Verify)

		// --- notifications ---
		api.GET("/notifications/counts", notificationH.Counts)
		api.GET("/notifications", notificationH.Index)
		api.PATCH("/notifications", notificationH.ReadAll)
		api.PATCH("/notifications/:id", notificationH.Read)
	}

	addr := ":" + cfg.App.Port
	log.Printf("starting go-gin server on %s", addr)
	if err = r.Run(addr); err != nil {
		log.Fatalf("server: %v", err)
	}
}
