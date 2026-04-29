package tests

import (
	"authorization-go-echo/internal/config"
	"authorization-go-echo/internal/handler"
	"authorization-go-echo/internal/infrastructure/cache"
	"authorization-go-echo/internal/infrastructure/db"
	"authorization-go-echo/internal/infrastructure/mail"
	"authorization-go-echo/internal/infrastructure/model"
	"authorization-go-echo/internal/infrastructure/persistence"
	"authorization-go-echo/internal/middleware"
	uclient "authorization-go-echo/internal/usecase/client"
	uauth "authorization-go-echo/internal/usecase/auth"
	ugate "authorization-go-echo/internal/usecase/gate"
	uinvitation "authorization-go-echo/internal/usecase/invitation"
	unotification "authorization-go-echo/internal/usecase/notification"
	ustaff "authorization-go-echo/internal/usecase/staff"
	"bytes"
	"context"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/hex"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"
	"time"

	"github.com/joho/godotenv"
	"github.com/labstack/echo/v4"
	redisclient "github.com/redis/go-redis/v9"
	"gorm.io/gorm"
)

var (
	testDB     *gorm.DB
	testCfg    *config.Config
	testRouter *echo.Echo
	testRDB    *redisclient.Client
)

func TestMain(m *testing.M) {
	_ = godotenv.Load("../.env.testing.local")
	_ = godotenv.Load("../.env.testing")

	testCfg = config.Load()

	var err error
	testDB, err = db.New(testCfg)
	if err != nil {
		panic(fmt.Sprintf("test db connect failed: %v", err))
	}

	testRDB = cache.New(testCfg)

	if err = runSchemaSql(); err != nil {
		panic(fmt.Sprintf("schema sql failed: %v", err))
	}

	testRouter = buildRouter()

	os.Exit(m.Run())
}

func buildRouter() *echo.Echo {
	rdb := cache.New(testCfg)
	gateCacheRepo := cache.NewRedisGateRepository(rdb, testCfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, testCfg)

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
		return uinvitation.NewInteractor(persistence.NewGormInvitationRepository(tx, testCfg.App.FrontendURL), invitationAuthRepo)
	}
	newNotifUC := func(tx *gorm.DB) *unotification.Interactor {
		return unotification.NewInteractor(
			persistence.NewGormNotificationRepository(tx),
			persistence.NewGormStaffRepository(tx),
		)
	}

	gateUC := ugate.NewInteractor(persistence.NewGormClientRepository(testDB), gateCacheRepo, testCfg)

	mailer := mail.NewMailer(testCfg.Mail)
	authH := handler.NewAuthHandler(testDB, newAuthUC, newInviteUC, testCfg)
	clientH := handler.NewClientHandler(testDB, newClientUC, newNotifUC, mailer)
	staffH := handler.NewStaffHandler(testDB, newStaffUC)
	gateH := handler.NewGateHandler(gateUC)
	notificationH := handler.NewNotificationHandler(testDB, newNotifUC, testCfg)
	adminInvH := handler.NewAdminInvitationHandler(testDB, newInviteUC)

	e := echo.New()
	e.HideBanner = true
	e.Use(middleware.ErrorHandler())

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

	api.GET("/admin/invitation", adminInvH.Index)
	api.GET("/admin/invitation/issue", adminInvH.Issue)

	api.GET("/gate/issue", gateH.Issue, middleware.ClientTokenAuth(newClientUC(testDB)))
	api.GET("/gate/client/:identifier/verify", gateH.Verify)

	api.GET("/notifications/counts", notificationH.Counts)
	api.GET("/notifications", notificationH.Index)
	api.PATCH("/notifications", notificationH.ReadAll)
	api.PATCH("/notifications/:id", notificationH.Read)

	return e
}

func runSchemaSql() error {
	sqlBytes, err := os.ReadFile("schema.sql")
	if err != nil {
		return fmt.Errorf("read schema.sql: %w", err)
	}
	statements := strings.Split(string(sqlBytes), ";")
	for _, stmt := range statements {
		stmt = strings.TrimSpace(stmt)
		if stmt == "" || strings.HasPrefix(stmt, "--") {
			continue
		}
		if err = testDB.Exec(stmt).Error; err != nil {
			return fmt.Errorf("exec statement: %w\nSQL: %s", err, stmt)
		}
	}
	return nil
}

func truncateTables(t *testing.T) {
	t.Helper()
	testDB.Exec("SET FOREIGN_KEY_CHECKS=0")
	testDB.Exec("TRUNCATE TABLE notifications")
	testDB.Exec("TRUNCATE TABLE invitations")
	testDB.Exec("TRUNCATE TABLE clients")
	testDB.Exec("TRUNCATE TABLE staffs")
	testDB.Exec("SET FOREIGN_KEY_CHECKS=1")
	testRDB.FlushDB(context.Background())
}

func do(method, path string, body interface{}, opts ...func(*http.Request)) *httptest.ResponseRecorder {
	var reqBody []byte
	if body != nil {
		reqBody, _ = json.Marshal(body)
	}
	req := httptest.NewRequest(method, path, bytes.NewReader(reqBody))
	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}
	for _, opt := range opts {
		opt(req)
	}
	w := httptest.NewRecorder()
	testRouter.ServeHTTP(w, req)
	return w
}

func withCookie(name, value string) func(*http.Request) {
	return func(req *http.Request) {
		req.AddCookie(&http.Cookie{Name: name, Value: value})
	}
}

func withBearer(token string) func(*http.Request) {
	return func(req *http.Request) {
		req.Header.Set("Authorization", "Bearer "+token)
	}
}

func parseBody(w *httptest.ResponseRecorder) map[string]interface{} {
	var result map[string]interface{}
	json.Unmarshal(w.Body.Bytes(), &result)
	return result
}

func createStaff(t *testing.T, overrides map[string]interface{}) *model.Staff {
	t.Helper()
	now := time.Now()
	staff := &model.Staff{
		Name:       "テストスタッフ",
		Email:      "staff@example.com",
		Provider:   1,
		ProviderID: "123456789",
		Role:       1,
		CreatedAt:  now,
		UpdatedAt:  now,
	}
	if overrides != nil {
		if v, ok := overrides["email"]; ok {
			staff.Email = v.(string)
		}
		if v, ok := overrides["name"]; ok {
			staff.Name = v.(string)
		}
		if v, ok := overrides["role"]; ok {
			staff.Role = v.(int)
		}
	}
	if err := testDB.Create(staff).Error; err != nil {
		t.Fatalf("createStaff: %v", err)
	}
	return staff
}

func createClient(t *testing.T, overrides map[string]interface{}) *model.Client {
	t.Helper()
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatalf("generateKey: %v", err)
	}
	privDER := x509.MarshalPKCS1PrivateKey(privateKey)
	privPEM := pem.EncodeToMemory(&pem.Block{Type: "RSA PRIVATE KEY", Bytes: privDER})
	pubDER, _ := x509.MarshalPKIXPublicKey(&privateKey.PublicKey)
	pubPEM := pem.EncodeToMemory(&pem.Block{Type: "PUBLIC KEY", Bytes: pubDER})

	tokenBytes := make([]byte, 32)
	rand.Read(tokenBytes)
	token := hex.EncodeToString(tokenBytes)

	now := time.Now()
	c := &model.Client{
		Name:        "テストクライアント",
		Identifier:  "test-client-001",
		PostCode:    "100-0001",
		Pref:        "東京都",
		City:        "千代田区",
		Address:     "千代田1-1",
		Tel:         "0312345678",
		Email:       "client@example.com",
		AccessToken: token,
		PrivateKey:  string(privPEM),
		PublicKey:   string(pubPEM),
		Fingerprint: "SHA256:test",
		Status:      1,
		CreatedAt:   now,
		UpdatedAt:   now,
	}
	if overrides != nil {
		if v, ok := overrides["identifier"]; ok {
			c.Identifier = v.(string)
		}
		if v, ok := overrides["name"]; ok {
			c.Name = v.(string)
		}
		if v, ok := overrides["email"]; ok {
			c.Email = v.(string)
		}
		if v, ok := overrides["status"]; ok {
			c.Status = v.(int)
		}
	}
	if err = testDB.Create(c).Error; err != nil {
		t.Fatalf("createClient: %v", err)
	}
	return c
}

func createInvitation(t *testing.T, token string) *model.Invitation {
	t.Helper()
	inv := &model.Invitation{Token: token}
	if err := testDB.Create(inv).Error; err != nil {
		t.Fatalf("createInvitation: %v", err)
	}
	return inv
}

func createNotification(t *testing.T, staffID uint, title string, overrides ...map[string]interface{}) *model.Notification {
	t.Helper()
	n := &model.Notification{
		StaffID:     staffID,
		MessageType: 1,
		Title:       title,
		Message:     "テスト通知本文",
	}
	if len(overrides) > 0 {
		if v, ok := overrides[0]["url"]; ok {
			s := v.(string)
			n.URL = &s
		}
	}
	if err := testDB.Create(n).Error; err != nil {
		t.Fatalf("createNotification: %v", err)
	}
	return n
}
