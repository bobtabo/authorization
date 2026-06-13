package tests

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
	"bytes"
	"context"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	stdsql "database/sql"
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
)

var (
	testDB     *ent.Client
	testRawDB  *stdsql.DB
	testCfg    *config.Config
	testRouter *echo.Echo
	testRDB    *redisclient.Client

	// cachedPrivPEM / cachedPubPEM: 2048-bit RSA 鍵生成は重いため、テスト全体で 1 回だけ生成して再利用する。
	cachedPrivPEM []byte
	cachedPubPEM  []byte
)

func TestMain(m *testing.M) {
	_ = godotenv.Load("../.env.testing.local")
	_ = godotenv.Load("../.env.testing")

	testCfg = config.Load()

	var err error
	testDB, testRawDB, err = db.New(testCfg)
	if err != nil {
		panic(fmt.Sprintf("test db connect failed: %v", err))
	}
	defer testDB.Close()

	testRDB = cache.New(testCfg)

	if err = runSchemaSql(); err != nil {
		panic(fmt.Sprintf("schema sql failed: %v", err))
	}

	testRouter = buildRouter()

	// RSA 鍵を 1 回だけ生成してキャッシュ（テストごとの生成コストを排除）
	pk, pkErr := rsa.GenerateKey(rand.Reader, 2048)
	if pkErr != nil {
		panic(fmt.Sprintf("pre-generate RSA key: %v", pkErr))
	}
	privDER := x509.MarshalPKCS1PrivateKey(pk)
	cachedPrivPEM = pem.EncodeToMemory(&pem.Block{Type: "RSA PRIVATE KEY", Bytes: privDER})
	pubDER, _ := x509.MarshalPKIXPublicKey(&pk.PublicKey)
	cachedPubPEM = pem.EncodeToMemory(&pem.Block{Type: "PUBLIC KEY", Bytes: pubDER})

	os.Exit(m.Run())
}

func buildRouter() *echo.Echo {
	rdb := cache.New(testCfg)
	gateCacheRepo := cache.NewRedisGateRepository(rdb, testCfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, testCfg)

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
		return uinvitation.NewInteractor(persistence.NewEntInvitationRepository(db, testCfg.App.FrontendURL), invitationAuthRepo)
	}
	newNotifUC := func(db *ent.Client) *unotification.Interactor {
		return unotification.NewInteractor(
			persistence.NewEntNotificationRepository(db),
			persistence.NewEntStaffRepository(db),
		)
	}

	gateUC := ugate.NewInteractor(persistence.NewEntClientRepository(testDB), nil, gateCacheRepo, testCfg)

	mailer := mail.NewMailer(testCfg.Mail, testCfg.AWS)
	authH := handler.NewAuthHandler(testDB, newAuthUC, newInviteUC, testCfg)
	clientH := handler.NewClientHandler(testDB, newClientUC, newNotifUC, mailer, nil, "")
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
	api.GET("/clients/:identifier/qr", clientH.GetQr)
	api.GET("/clients/:identifier/info", clientH.GetInfo)
	api.PATCH("/clients/:identifier/start", clientH.Start)
	api.PATCH("/clients/:identifier/stop", clientH.Stop)

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
		if _, err = testRawDB.Exec(stmt); err != nil {
			return fmt.Errorf("exec statement: %w\nSQL: %s", err, stmt)
		}
	}
	return nil
}

func truncateTables(t *testing.T) {
	t.Helper()
	testRawDB.Exec("SET FOREIGN_KEY_CHECKS=0")
	testRawDB.Exec("TRUNCATE TABLE notifications")
	testRawDB.Exec("TRUNCATE TABLE invitations")
	testRawDB.Exec("TRUNCATE TABLE clients")
	testRawDB.Exec("TRUNCATE TABLE staffs")
	testRawDB.Exec("SET FOREIGN_KEY_CHECKS=1")
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

func createStaff(t *testing.T, overrides map[string]interface{}) *ent.Staff {
	t.Helper()
	ctx := context.Background()
	now := time.Now()
	q := testDB.Staff.Create().
		SetName("テストスタッフ").
		SetEmail("staff@example.com").
		SetProvider(1).
		SetProviderID("123456789").
		SetRole(1).
		SetCreatedAt(now).
		SetUpdatedAt(now)
	if overrides != nil {
		if v, ok := overrides["email"]; ok {
			q = q.SetEmail(v.(string))
		}
		if v, ok := overrides["name"]; ok {
			q = q.SetName(v.(string))
		}
		if v, ok := overrides["role"]; ok {
			q = q.SetRole(v.(int))
		}
	}
	s, err := q.Save(ctx)
	if err != nil {
		t.Fatalf("createStaff: %v", err)
	}
	return s
}

func createClient(t *testing.T, overrides map[string]interface{}) *ent.AppClient {
	t.Helper()
	ctx := context.Background()

	tokenBytes := make([]byte, 32)
	rand.Read(tokenBytes)
	token := hex.EncodeToString(tokenBytes)

	now := time.Now()
	q := testDB.AppClient.Create().
		SetName("テストクライアント").
		SetIdentifier("test-client-001").
		SetPostCode("100-0001").
		SetPref("東京都").
		SetCity("千代田区").
		SetAddress("千代田1-1").
		SetTel("0312345678").
		SetEmail("client@example.com").
		SetAccessToken(token).
		SetPrivateKey(string(cachedPrivPEM)).
		SetPublicKey(string(cachedPubPEM)).
		SetFingerprint("SHA256:test").
		SetStatus(1).
		SetCreatedAt(now).
		SetUpdatedAt(now)
	if overrides != nil {
		if v, ok := overrides["identifier"]; ok {
			q = q.SetIdentifier(v.(string))
		}
		if v, ok := overrides["name"]; ok {
			q = q.SetName(v.(string))
		}
		if v, ok := overrides["email"]; ok {
			q = q.SetEmail(v.(string))
		}
		if v, ok := overrides["status"]; ok {
			q = q.SetStatus(v.(int))
		}
	}
	c, err := q.Save(ctx)
	if err != nil {
		t.Fatalf("createClient: %v", err)
	}
	return c
}

func createInvitation(t *testing.T, token string) *ent.Invitation {
	t.Helper()
	ctx := context.Background()
	now := time.Now()
	inv, err := testDB.Invitation.Create().
		SetToken(token).
		SetCreatedAt(now).
		SetUpdatedAt(now).
		Save(ctx)
	if err != nil {
		t.Fatalf("createInvitation: %v", err)
	}
	return inv
}

func createNotification(t *testing.T, staffID uint, title string, overrides ...map[string]interface{}) *ent.Notification {
	t.Helper()
	ctx := context.Background()
	now := time.Now()
	q := testDB.Notification.Create().
		SetStaffID(staffID).
		SetMessageType(1).
		SetTitle(title).
		SetMessage("テスト通知本文").
		SetCreatedAt(now).
		SetUpdatedAt(now)
	if len(overrides) > 0 {
		if v, ok := overrides[0]["url"]; ok {
			q = q.SetURL(v.(string))
		}
	}
	n, err := q.Save(ctx)
	if err != nil {
		t.Fatalf("createNotification: %v", err)
	}
	return n
}
