package tests

import (
	"authorization-go/internal/handler"
	"fmt"
	"net/http"
	"testing"
	"time"
)

func TestAuth_GetMyProfile(t *testing.T) {
	truncateTables(t)

	t.Run("認証済みでプロフィールが取得できる", func(t *testing.T) {
		staff := createStaff(t, nil)
		w := do(http.MethodGet, "/api/auth/me", nil,
			withCookie("staff_id", signStaffCookie(staff.ID)))
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

	t.Run("署名の無い偽造クッキーでは401が返る", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "forge-1@example.com"})
		// 署名を付けず staff_id をそのまま設定した「偽造」クッキー。
		w := do(http.MethodGet, "/api/auth/me", nil,
			withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("署名が不正なクッキーでは401が返る", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "forge-2@example.com"})
		signed := signStaffCookie(staff.ID)
		// 末尾の1文字を必ず異なる値に置き換える（元の値と偶然一致すると署名が
		// 変わらずテストが不安定になるため）。
		replacement := byte('0')
		if signed[len(signed)-1] == replacement {
			replacement = '1'
		}
		tampered := signed[:len(signed)-1] + string(replacement)
		w := do(http.MethodGet, "/api/auth/me", nil, withCookie("staff_id", tampered))
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("別のシークレットで署名されたクッキーでは401が返る", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "forge-3@example.com"})
		forged := handler.SignStaffID(staff.ID, "attacker-controlled-secret", time.Hour)
		w := do(http.MethodGet, "/api/auth/me", nil, withCookie("staff_id", forged))
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("有効期限切れの署名済みクッキーでは401が返る", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "forge-4@example.com"})
		// 署名自体は正しいが、Max-Ageが切れた後に手動でCookieヘッダーを
		// 再送した状況を再現する（署名対象に有効期限を含めていないと防げない）。
		expired := handler.SignStaffID(staff.ID, testCfg.App.StaffCookieSecret, -time.Hour)
		w := do(http.MethodGet, "/api/auth/me", nil, withCookie("staff_id", expired))
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d: %s", w.Code, w.Body.String())
		}
	})
}

func TestAuth_Login(t *testing.T) {
	truncateTables(t)

	t.Run("認証済みでログイン情報が取得できる", func(t *testing.T) {
		staff := createStaff(t, nil)
		w := do(http.MethodGet, "/api/auth/login", nil,
			withCookie("staff_id", signStaffCookie(staff.ID)))
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
