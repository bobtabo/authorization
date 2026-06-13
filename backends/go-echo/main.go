package main

import (
	"authorization-go-echo/ent"
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
)

func main() {
	cfg := config.Load()

	database, rawDB, err := db.New(cfg)
	if err != nil {
		log.Fatalf("db connect: %v", err)
	}
	defer database.Close()

	rdb := cache.New(cfg)

	gateCacheRepo := cache.NewRedisGateRepository(rdb, cfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, cfg)

	newAuthUC := func(db *ent.Client) *uauth.Interactor {
		return uauth.NewInteractor(persistence.NewEntStaffRepository(db), invitationAuthRepo)
	}
	newClientUC := func(db *ent.Client) *uclient.Interactor {
		return uclient.NewInteractor(persistence.NewEntClientRepository(db))
	}
	newStaffUC := func(db *ent.Client) *ustaff.Interactor {
		return ustaff.NewInteractor(persistence.NewEntStaffRepository(db))
	}
	newInviteUC := func(db *ent.Client) *uinvitation.Interactor {
		return uinvitation.NewInteractor(persistence.NewEntInvitationRepository(db, cfg.App.FrontendURL), invitationAuthRepo)
	}
	newNotifUC := func(db *ent.Client) *unotification.Interactor {
		return unotification.NewInteractor(
			persistence.NewEntNotificationRepository(db),
			persistence.NewEntStaffRepository(db),
		)
	}

	jwtHistoryRepo := persistence.NewSQLJwtHistoryRepository(rawDB)
	gateUC := ugate.NewInteractor(persistence.NewEntClientRepository(database), jwtHistoryRepo, gateCacheRepo, cfg)

	mailer := mail.NewMailer(cfg.Mail, cfg.AWS)

	authH := handler.NewAuthHandler(database, newAuthUC, newInviteUC, cfg)
	clientH := handler.NewClientHandler(database, newClientUC, newNotifUC, mailer, jwtHistoryRepo, cfg.App.FrontendURL)
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
	e.GET("/auth/github/redirect", authH.GithubRedirect)
	e.GET("/auth/github/callback", authH.GithubCallback)

	api := e.Group("/api")

	api.GET("/auth/me", authH.GetMyProfile)
	api.GET("/auth/login", authH.Login)
	api.GET("/auth/logout", authH.Logout)
	api.GET("/auth/invitation/:token", authH.Invitation)

	api.GET("/clients", clientH.Index)
	api.POST("/clients/store", clientH.Store)
	api.PUT("/clients/:id/update", clientH.Update)
	api.GET("/clients/:id", clientH.Show)
	api.GET("/clients/:id/jwt-histories", clientH.JwtHistories)
	api.DELETE("/clients/:id/delete", clientH.Destroy)
	api.GET("/clients/:identifier/qr", clientH.GetQr)
	api.GET("/clients/:identifier/info", clientH.GetInfo)
	api.PATCH("/clients/:identifier/start", clientH.Start)
	api.PATCH("/clients/:identifier/stop", clientH.Stop)

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
