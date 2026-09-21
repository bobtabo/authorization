package tests

import (
	"fmt"
	"net/http"
	"net/url"
	"strings"
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

func TestAuth_OAuthState(t *testing.T) {
	t.Run("認可開始時にnonceクッキーを発行しstateに含める", func(t *testing.T) {
		w := do(http.MethodGet, "/auth/google/redirect?token=inv-token", nil)
		if w.Code != http.StatusTemporaryRedirect {
			t.Fatalf("want 307, got %d", w.Code)
		}
		var nonce string
		for _, c := range w.Result().Cookies() {
			if c.Name == "oauth_state" {
				nonce = c.Value
				if !c.HttpOnly {
					t.Error("oauth_state cookie must be HttpOnly")
				}
			}
		}
		if nonce == "" {
			t.Fatal("oauth_state cookie not set")
		}
		loc, err := url.Parse(w.Header().Get("Location"))
		if err != nil {
			t.Fatal(err)
		}
		want := testCfg.OAuth.Runtime + "|" + nonce + "|inv-token"
		if got := loc.Query().Get("state"); got != want {
			t.Errorf("state: want %q, got %q", want, got)
		}
	})

	t.Run("nonceクッキーが無い場合は400エラーページへリダイレクトする", func(t *testing.T) {
		w := do(http.MethodGet, "/auth/google/callback?code=x&state=go-beego%7Cabc", nil)
		if w.Code != http.StatusTemporaryRedirect {
			t.Fatalf("want 307, got %d", w.Code)
		}
		if loc := w.Header().Get("Location"); !strings.HasSuffix(loc, "/error?code=400") {
			t.Errorf("want redirect to /error?code=400, got %s", loc)
		}
	})

	t.Run("nonceが一致しない場合は400エラーページへリダイレクトする", func(t *testing.T) {
		w := do(http.MethodGet, "/auth/google/callback?code=x&state=go-beego%7Cabc", nil,
			withCookie("oauth_state", "xyz"))
		if loc := w.Header().Get("Location"); !strings.HasSuffix(loc, "/error?code=400") {
			t.Errorf("want redirect to /error?code=400, got %s", loc)
		}
	})

	t.Run("stateにnonceセグメントが無い場合は400エラーページへリダイレクトする", func(t *testing.T) {
		w := do(http.MethodGet, "/auth/google/callback?code=x&state=go-beego", nil,
			withCookie("oauth_state", "abc"))
		if loc := w.Header().Get("Location"); !strings.HasSuffix(loc, "/error?code=400") {
			t.Errorf("want redirect to /error?code=400, got %s", loc)
		}
	})
}
