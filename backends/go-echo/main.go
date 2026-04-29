package main

import (
	"authorization-go-echo/internal/config"
	"authorization-go-echo/internal/handler"
	"authorization-go-echo/internal/infrastructure/cache"
	"authorization-go-echo/internal/infrastructure/db"
	"authorization-go-echo/internal/infrastructure/mail"
	"authorization-go-echo/internal/infrastructure/persistence"
	"authorization-go-echo/internal/middleware"
	uauth "authorization-go-echo/internal/usecase/auth"
	uclient "authorization-go-echo/internal/usecase/client"
	ugate "authorization-go-echo/internal/usecase/gate"
	uinvitation "authorization-go-echo/internal/usecase/invitation"
	unotification "authorization-go-echo/internal/usecase/notification"
	ustaff "authorization-go-echo/internal/usecase/staff"
	"log"
	"net/http"

	"github.com/labstack/echo/v4"
	echomw "github.com/labstack/echo/v4/middleware"
	"gorm.io/gorm"
)

func main() {
	cfg := config.Load()

	database, err := db.New(cfg)
	if err != nil {
		log.Fatalf("db connect: %v", err)
	}

	rdb := cache.New(cfg)

	gateCacheRepo := cache.NewRedisGateRepository(rdb, cfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, cfg)

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

	gateUC := ugate.NewInteractor(persistence.NewGormClientRepository(database), gateCacheRepo, cfg)

	mailer := mail.NewMailer(cfg.Mail)

	authH := handler.NewAuthHandler(database, newAuthUC, newInviteUC, cfg)
	clientH := handler.NewClientHandler(database, newClientUC, newNotifUC, mailer)
	staffH := handler.NewStaffHandler(database, newStaffUC)
	adminInvitationH := handler.NewAdminInvitationHandler(database, newInviteUC)
	gateH := handler.NewGateHandler(gateUC)
	notificationH := handler.NewNotificationHandler(database, newNotifUC, cfg)

	e := echo.New()
	e.HideBanner = true
	e.Use(echomw.Logger())
	e.Use(echomw.Recover())
	e.Use(middleware.ErrorHandler())

	e.Use(func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			c.Response().Header().Set("Access-Control-Allow-Origin", cfg.App.FrontendURL)
			c.Response().Header().Set("Access-Control-Allow-Credentials", "true")
			c.Response().Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
			c.Response().Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
			if c.Request().Method == http.MethodOptions {
				return c.NoContent(http.StatusNoContent)
			}
			return next(c)
		}
	})

	e.GET("/auth/google/redirect", authH.GoogleRedirect)
	e.GET("/auth/google/callback", authH.GoogleCallback)

	api := e.Group("/api")

	api.GET("/auth/me", authH.GetMyProfile)
	api.GET("/auth/login", authH.Login)
	api.GET("/auth/logout", authH.Logout)
	api.GET("/auth/invitation/:token", authH.Invitation)

	api.GET("/clients", clientH.Index)
	api.POST("/clients/store", clientH.Store)
	api.PUT("/clients/:id/update", clientH.Update)
	api.GET("/clients/:id", clientH.Show)
	api.DELETE("/clients/:id/delete", clientH.Destroy)

	api.GET("/staffs", staffH.Index)
	api.PATCH("/staffs/:id/updateRole", staffH.UpdateRole)
	api.PATCH("/staffs/:id/restore", staffH.Restore)
	api.DELETE("/staffs/:id/delete", staffH.Destroy)

	api.GET("/admin/invitation", adminInvitationH.Index)
	api.GET("/admin/invitation/issue", adminInvitationH.Issue)

	api.GET("/gate/issue",
		gateH.Issue,
		middleware.ClientTokenAuth(newClientUC(database)),
	)
	api.GET("/gate/client/:identifier/verify", gateH.Verify)

	api.GET("/notifications/counts", notificationH.Counts)
	api.GET("/notifications", notificationH.Index)
	api.PATCH("/notifications", notificationH.ReadAll)
	api.PATCH("/notifications/:id", notificationH.Read)

	addr := ":" + cfg.App.Port
	log.Printf("starting go-echo server on %s", addr)
	if err = e.Start(addr); err != nil {
		log.Fatalf("server: %v", err)
	}
}
