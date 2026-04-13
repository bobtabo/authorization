package tests

import (
	"fmt"
	"net/http"
	"testing"
)

func TestAuth_GetMyProfile(t *testing.T) {
	truncateTables(t)

	t.Run("認証済みでプロフィールが取得できる", func(t *testing.T) {
		staff := createStaff(t, nil)
		w := do(http.MethodGet, "/api/auth/me", nil,
			withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["staff_id"] == nil {
			t.Error("staff_id not found in response")
		}
		if body["name"] == nil {
			t.Error("name not found in response")
		}
	})

	t.Run("未認証で401が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/auth/me", nil)
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})
}

func TestAuth_Login(t *testing.T) {
	truncateTables(t)

	t.Run("認証済みでログイン情報が取得できる", func(t *testing.T) {
		staff := createStaff(t, nil)
		w := do(http.MethodGet, "/api/auth/login", nil,
			withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["staff_id"] == nil {
			t.Error("staff_id not found in response")
		}
	})

	t.Run("未認証で401が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/auth/login", nil)
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})
}

func TestAuth_Logout(t *testing.T) {
	t.Run("ログアウトが成功する", func(t *testing.T) {
		w := do(http.MethodGet, "/api/auth/logout", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})
}

func TestAuth_Invitation(t *testing.T) {
	truncateTables(t)

	t.Run("有効なトークンで招待情報が取得できる", func(t *testing.T) {
		inv := createInvitation(t, "valid-test-token")
		w := do(http.MethodGet, "/api/auth/invitation/"+inv.Token, nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["token"] == nil {
			t.Error("token not found in response")
		}
	})
}
