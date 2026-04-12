package main

import (
	"authorization-go/internal/config"
	"authorization-go/internal/handler"
	"authorization-go/internal/infrastructure/cache"
	"authorization-go/internal/infrastructure/db"
	"authorization-go/internal/middleware"
	"authorization-go/internal/repository"
	"authorization-go/internal/service"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
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

	// --- Repositories ---
	clientRepo := repository.NewClientRepository(database)
	staffRepo := repository.NewStaffRepository(database)
	invitationRepo := repository.NewInvitationRepository(database, cfg.App.FrontendURL)
	notificationRepo := repository.NewNotificationRepository(database)
	gateCacheRepo := repository.NewGateCacheRepository(rdb, cfg)

	// --- Services ---
	authSvc := service.NewAuthService(staffRepo)
	clientSvc := service.NewClientService(clientRepo)
	staffSvc := service.NewStaffService(staffRepo)
	invitationSvc := service.NewInvitationService(invitationRepo)
	gateSvc := service.NewGateService(clientRepo, gateCacheRepo, cfg)
	notificationSvc := service.NewNotificationService(notificationRepo, staffRepo)

	// --- Handlers ---
	authH := handler.NewAuthHandler(authSvc, invitationSvc, cfg)
	clientH := handler.NewClientHandler(clientSvc, notificationSvc)
	staffH := handler.NewStaffHandler(staffSvc)
	invitationH := handler.NewInvitationHandler(invitationSvc)
	gateH := handler.NewGateHandler(gateSvc)
	notificationH := handler.NewNotificationHandler(notificationSvc, cfg)

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

		// --- staffs ---
		api.GET("/staffs", staffH.Index)
		api.PATCH("/staffs/:id/updateRole", staffH.UpdateRole)
		api.PATCH("/staffs/:id/restore", staffH.Restore)
		api.DELETE("/staffs/:id/delete", staffH.Destroy)

		// --- invitation ---
		api.GET("/invitation/issue", invitationH.Issue)
		api.GET("/invitation", invitationH.Index)

		// --- gate ---
		api.GET("/gate/issue",
			middleware.ClientTokenAuth(clientSvc),
			gateH.Issue,
		)
		api.GET("/gate/client/:identifier/verify", gateH.Verify)

		// --- notifications ---
		api.GET("/notifications/counts", notificationH.Counts)
		api.GET("/notifications", notificationH.Index)
		api.POST("/notifications", notificationH.Store)
		api.PATCH("/notifications", notificationH.ReadAll)
		api.PATCH("/notifications/:id", notificationH.Read)
	}

	addr := ":" + cfg.App.Port
	log.Printf("starting go-gin server on %s", addr)
	if err = r.Run(addr); err != nil {
		log.Fatalf("server: %v", err)
	}
}
