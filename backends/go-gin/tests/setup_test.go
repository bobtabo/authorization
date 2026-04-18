// Package tests はGo/Ginバックエンドのフィーチャーテストです。
// 実際のMySQL（authorization_test DB）を使い、HTTP レベルで各エンドポイントを検証します。
package tests

import (
	"authorization-go/internal/config"
	"authorization-go/internal/handler"
	"authorization-go/internal/infrastructure/cache"
	"authorization-go/internal/infrastructure/db"
	"authorization-go/internal/infrastructure/mail"
	"authorization-go/internal/infrastructure/model"
	"authorization-go/internal/infrastructure/persistence"
	"authorization-go/internal/middleware"
	uclient "authorization-go/internal/usecase/client"
	uauth "authorization-go/internal/usecase/auth"
	ugate "authorization-go/internal/usecase/gate"
	uinvitation "authorization-go/internal/usecase/invitation"
	unotification "authorization-go/internal/usecase/notification"
	ustaff "authorization-go/internal/usecase/staff"
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

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	redisclient "github.com/redis/go-redis/v9"
	"gorm.io/gorm"
)

var (
	testDB     *gorm.DB
	testCfg    *config.Config
	testRouter *gin.Engine
	testRDB    *redisclient.Client
)

func TestMain(m *testing.M) {
	// ローカルコンテナ用（host.docker.internal）を優先、なければ CI 用（127.0.0.1）
	// godotenv.Load は既存の env var を上書きしないため CI の env vars が最優先される
	_ = godotenv.Load("../.env.testing.local")
	_ = godotenv.Load("../.env.testing")

	testCfg = config.Load()

	var err error
	testDB, err = db.New(testCfg)
	if err != nil {
		panic(fmt.Sprintf("test db connect failed: %v", err))
	}

	testRDB = cache.New(testCfg)

	// テーブルを schema.sql から作成（AutoMigrate は TEXT 型になるため使わない）
	if err = runSchemaSql(); err != nil {
		panic(fmt.Sprintf("schema sql failed: %v", err))
	}

	testRouter = buildRouter()

	os.Exit(m.Run())
}

// buildRouter はテスト用の Gin ルーターを構築します。
func buildRouter() *gin.Engine {
	rdb := cache.New(testCfg)

	clientRepo := persistence.NewGormClientRepository(testDB)
	staffRepo := persistence.NewGormStaffRepository(testDB)
	invitationRepo := persistence.NewGormInvitationRepository(testDB, testCfg.App.FrontendURL)
	notificationRepo := persistence.NewGormNotificationRepository(testDB)
	gateCacheRepo := cache.NewGateCacheRepository(rdb, testCfg)

	authUC := uauth.NewInteractor(staffRepo)
	clientUC := uclient.NewInteractor(clientRepo)
	staffUC := ustaff.NewInteractor(staffRepo)
	invitationUC := uinvitation.NewInteractor(invitationRepo)
	gateUC := ugate.NewInteractor(clientRepo, gateCacheRepo, testCfg)
	notificationUC := unotification.NewInteractor(notificationRepo, staffRepo)

	mailer := mail.NewMailer(testCfg.Mail)
	authH := handler.NewAuthHandler(authUC, invitationUC, testCfg)
	clientH := handler.NewClientHandler(clientUC, notificationUC, mailer)
	staffH := handler.NewStaffHandler(staffUC)
	invitationH := handler.NewInvitationHandler(invitationUC)
	gateH := handler.NewGateHandler(gateUC)
	notificationH := handler.NewNotificationHandler(notificationUC, testCfg)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.Use(middleware.ErrorHandler())

	r.GET("/auth/google/redirect", authH.GoogleRedirect)
	r.GET("/auth/google/callback", authH.GoogleCallback)

	api := r.Group("/api")
	{
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

		api.GET("/invitation/issue", invitationH.Issue)
		api.GET("/invitation", invitationH.Index)

		api.GET("/gate/issue", middleware.ClientTokenAuth(clientUC), gateH.Issue)
		api.GET("/gate/client/:identifier/verify", gateH.Verify)

		api.GET("/notifications/counts", notificationH.Counts)
		api.GET("/notifications", notificationH.Index)
		api.PATCH("/notifications", notificationH.ReadAll)
		api.PATCH("/notifications/:id", notificationH.Read)
	}

	return r
}

// runSchemaSql は schema.sql を読み込んでテーブルを作成します。
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

// truncateTables は全テストテーブルと Redis キャッシュをクリアします。
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

// do はテスト用HTTPリクエストを実行します。
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

// withCookie はリクエストにクッキーを追加するオプションです。
func withCookie(name, value string) func(*http.Request) {
	return func(req *http.Request) {
		req.AddCookie(&http.Cookie{Name: name, Value: value})
	}
}

// withBearer はリクエストに Bearer トークンを追加するオプションです。
func withBearer(token string) func(*http.Request) {
	return func(req *http.Request) {
		req.Header.Set("Authorization", "Bearer "+token)
	}
}

// withHeader はリクエストにヘッダーを追加するオプションです。
func withHeader(key, value string) func(*http.Request) {
	return func(req *http.Request) {
		req.Header.Set(key, value)
	}
}

// parseBody はレスポンスボディを map に変換します。
func parseBody(w *httptest.ResponseRecorder) map[string]interface{} {
	var result map[string]interface{}
	json.Unmarshal(w.Body.Bytes(), &result)
	return result
}

// ---------- テストデータ生成ヘルパー ----------

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
	if v, ok := overrides["email"]; ok {
		staff.Email = v.(string)
	}
	if v, ok := overrides["name"]; ok {
		staff.Name = v.(string)
	}
	if v, ok := overrides["role"]; ok {
		staff.Role = v.(int)
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
