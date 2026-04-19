package tests

import (
	"net/http"
	"testing"
)

func TestAdminInvitation_Issue(t *testing.T) {
	truncateTables(t)

	t.Run("招待URLが発行できる", func(t *testing.T) {
		w := do(http.MethodGet, "/api/admin/invitation/issue", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["url"] == nil {
			t.Error("url not found in response")
		}
		if body["token"] == nil {
			t.Error("token not found in response")
		}
	})

	t.Run("再発行で新しいトークンが返る", func(t *testing.T) {
		createInvitation(t, "old-token")
		w := do(http.MethodGet, "/api/admin/invitation/issue", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["token"] == "old-token" {
			t.Error("expected new token, got old-token")
		}
	})
}
