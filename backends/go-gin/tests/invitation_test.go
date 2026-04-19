package tests

import (
	"net/http"
	"testing"
)

func TestInvitation_Index(t *testing.T) {
	truncateTables(t)

	t.Run("現在の招待URLが取得できる", func(t *testing.T) {
		inv := createInvitation(t, "current-test-token")
		w := do(http.MethodGet, "/api/invitation", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["token"] != inv.Token {
			t.Errorf("want token=%s, got %v", inv.Token, body["token"])
		}
	})
}
