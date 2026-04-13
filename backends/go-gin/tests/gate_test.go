package tests

import (
	"fmt"
	"net/http"
	"testing"
)

func TestGate_Issue(t *testing.T) {
	truncateTables(t)

	t.Run("JWTが発行できる", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"status": 2})
		w := do(http.MethodGet, "/api/gate/issue?member=member-001", nil,
			withBearer(c.AccessToken))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["token"] == nil {
			t.Error("token not found in response")
		}
	})

	t.Run("利用中以外のクライアントで401が返る", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "c-suspended", "email": "suspended@example.com", "status": 3})
		w := do(http.MethodGet, "/api/gate/issue?member=member-001", nil,
			withBearer(c.AccessToken))
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})

	t.Run("memberパラメーター未指定で400が返る", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "c-for-member-test", "email": "mt@example.com", "status": 2})
		w := do(http.MethodGet, "/api/gate/issue", nil,
			withBearer(c.AccessToken))
		if w.Code != http.StatusBadRequest {
			t.Errorf("want 400, got %d", w.Code)
		}
	})

	t.Run("無効なトークンで401が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/gate/issue?member=member-001", nil,
			withBearer("invalid-token"))
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})

	t.Run("Bearerトークンなしで401が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/gate/issue?member=member-001", nil)
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})
}

func TestGate_Verify(t *testing.T) {
	truncateTables(t)

	t.Run("JWTが検証できる", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"status": 2})

		// JWT を発行
		issueW := do(http.MethodGet, "/api/gate/issue?member=member-001", nil,
			withBearer(c.AccessToken))
		if issueW.Code != http.StatusOK {
			t.Fatalf("issue failed: %d %s", issueW.Code, issueW.Body.String())
		}
		jwtToken := parseBody(issueW)["token"].(string)

		// JWT を検証
		verifyURL := fmt.Sprintf("/api/gate/client/%s/verify?token=%s", c.Identifier, jwtToken)
		w := do(http.MethodGet, verifyURL, nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("tokenパラメーター未指定で400が返る", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "c-verify", "email": "v@example.com", "status": 2})
		w := do(http.MethodGet, fmt.Sprintf("/api/gate/client/%s/verify", c.Identifier), nil)
		if w.Code != http.StatusBadRequest {
			t.Errorf("want 400, got %d", w.Code)
		}
	})

	t.Run("存在しないidentifierで403が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/gate/client/unknown-client/verify?token=dummy", nil)
		if w.Code != http.StatusForbidden {
			t.Errorf("want 403, got %d", w.Code)
		}
	})

	t.Run("無効なJWTで401が返る", func(t *testing.T) {
		createClient(t, map[string]interface{}{"identifier": "jwt-test-client", "email": "jwt@example.com", "status": 2})
		w := do(http.MethodGet, "/api/gate/client/jwt-test-client/verify?token=invalid.jwt.token", nil)
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})
}
