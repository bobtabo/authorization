package tests

import (
	"authorization-go-beego/internal/config"
	"authorization-go-beego/internal/handler"
	"authorization-go-beego/internal/infrastructure/cache"
	"authorization-go-beego/internal/infrastructure/db"
	"authorization-go-beego/internal/infrastructure/mail"
	"authorization-go-beego/internal/infrastructure/model"
	"authorization-go-beego/internal/infrastructure/persistence"
	"authorization-go-beego/internal/middleware"
	uauth "authorization-go-beego/internal/usecase/auth"
	uclient "authorization-go-beego/internal/usecase/client"
	ugate "authorization-go-beego/internal/usecase/gate"
	uinvitation "authorization-go-beego/internal/usecase/invitation"
	unotification "authorization-go-beego/internal/usecase/notification"
	ustaff "authorization-go-beego/internal/usecase/staff"
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

	"github.com/beego/beego/v2/client/orm"
	"github.com/beego/beego/v2/server/web"
	"github.com/joho/godotenv"
	redisclient "github.com/redis/go-redis/v9"
)

var (
	testOrmer   orm.Ormer
	testCfg     *config.Config
	testHandler http.Handler
	testRDB     *redisclient.Client

	// cachedPrivPEM / cachedPubPEM: 2048-bit RSA 鍵生成は重いため、テスト全体で 1 回だけ生成して再利用する。
	cachedPrivPEM []byte
	cachedPubPEM  []byte
)

func TestMain(m *testing.M) {
	_ = godotenv.Load("../.env.testing.local")
	_ = godotenv.Load("../.env.testing")

	testCfg = config.Load()

	var err error
	testOrmer, err = db.New(testCfg)
	if err != nil {
		panic(fmt.Sprintf("test db connect failed: %v", err))
	}

	testRDB = cache.New(testCfg)

	if err = runSchemaSql(); err != nil {
		panic(fmt.Sprintf("schema sql failed: %v", err))
	}

	testHandler = buildRouter()

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

func buildRouter() http.Handler {
	rdb := cache.New(testCfg)
	gateCacheRepo := cache.NewRedisGateRepository(rdb, testCfg)
	invitationAuthRepo := cache.NewRedisInvitationAuthRepository(rdb, testCfg)

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
		return uinvitation.NewInteractor(persistence.NewOrmInvitationRepository(o, testCfg.App.FrontendURL), invitationAuthRepo)
	}
	newNotifUC := func(o persistence.QueryOrmer) *unotification.Interactor {
		return unotification.NewInteractor(
			persistence.NewOrmNotificationRepository(o),
			persistence.NewOrmStaffRepository(o),
		)
	}

	gateUC := ugate.NewInteractor(persistence.NewOrmClientRepository(testOrmer), nil, gateCacheRepo, testCfg)

	mailer := mail.NewMailer(testCfg.Mail)
	authH := handler.NewAuthHandler(testOrmer, newAuthUC, newInviteUC, testCfg)
	clientH := handler.NewClientHandler(testOrmer, newClientUC, newNotifUC, mailer, nil, "")
	staffH := handler.NewStaffHandler(testOrmer, newStaffUC)
	gateH := handler.NewGateHandler(gateUC)
	notificationH := handler.NewNotificationHandler(testOrmer, newNotifUC, testCfg)
	adminInvH := handler.NewAdminInvitationHandler(testOrmer, newInviteUC)

	web.BConfig.CopyRequestBody = true
	web.BConfig.WebConfig.AutoRender = false
	web.BConfig.Log.AccessLogs = false

	web.InsertFilter("/api/gate/issue", web.BeforeExec,
		middleware.ClientTokenAuth(newClientUC(testOrmer)),
		web.WithReturnOnOutput(true),
	)

	web.Get("/auth/google/redirect", authH.GoogleRedirect)
	web.Get("/auth/google/callback", authH.GoogleCallback)

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

	web.Get("/api/admin/invitation", adminInvH.Index)
	web.Get("/api/admin/invitation/issue", adminInvH.Issue)

	web.Get("/api/gate/issue", gateH.Issue)
	web.Get("/api/gate/client/:identifier/verify", gateH.Verify)

	web.Get("/api/notifications/counts", notificationH.Counts)
	web.Get("/api/notifications", notificationH.Index)
	web.Patch("/api/notifications", notificationH.ReadAll)
	web.Patch("/api/notifications/:id", notificationH.Read)

	return web.BeeApp.Handlers
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
		if _, err = testOrmer.Raw(stmt).Exec(); err != nil {
			return fmt.Errorf("exec statement: %w\nSQL: %s", err, stmt)
		}
	}
	return nil
}

func truncateTables(t *testing.T) {
	t.Helper()
	testOrmer.Raw("SET FOREIGN_KEY_CHECKS=0").Exec()
	testOrmer.Raw("TRUNCATE TABLE notifications").Exec()
	testOrmer.Raw("TRUNCATE TABLE invitations").Exec()
	testOrmer.Raw("TRUNCATE TABLE clients").Exec()
	testOrmer.Raw("TRUNCATE TABLE staffs").Exec()
	testOrmer.Raw("SET FOREIGN_KEY_CHECKS=1").Exec()
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
	testHandler.ServeHTTP(w, req)
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
	if _, err := testOrmer.Insert(staff); err != nil {
		t.Fatalf("createStaff: %v", err)
	}
	return staff
}

func createClient(t *testing.T, overrides map[string]interface{}) *model.Client {
	t.Helper()

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
		PrivateKey:  string(cachedPrivPEM),
		PublicKey:   string(cachedPubPEM),
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
	if _, err = testOrmer.Insert(c); err != nil {
		t.Fatalf("createClient: %v", err)
	}
	return c
}

func createInvitation(t *testing.T, token string, role ...int) *model.Invitation {
	t.Helper()
	r := 2
	if len(role) > 0 {
		r = role[0]
	}
	now := time.Now()
	inv := &model.Invitation{
		Token:     token,
		Role:      r,
		CreatedAt: now,
		UpdatedAt: now,
	}
	if _, err := testOrmer.Insert(inv); err != nil {
		t.Fatalf("createInvitation: %v", err)
	}
	return inv
}

func createNotification(t *testing.T, staffID uint, title string, overrides ...map[string]interface{}) *model.Notification {
	t.Helper()
	now := time.Now()
	n := &model.Notification{
		StaffID:     staffID,
		MessageType: 1,
		Title:       title,
		Message:     "テスト通知本文",
		CreatedAt:   now,
		UpdatedAt:   now,
	}
	if len(overrides) > 0 {
		if v, ok := overrides[0]["url"]; ok {
			s := v.(string)
			n.URL = &s
		}
	}
	if _, err := testOrmer.Insert(n); err != nil {
		t.Fatalf("createNotification: %v", err)
	}
	return n
}
