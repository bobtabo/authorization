package tests

import (
	"fmt"
	"net/http"
	"testing"
)

func TestClient_Index(t *testing.T) {
	truncateTables(t)

	t.Run("クライアント一覧が取得できる", func(t *testing.T) {
		createClient(t, map[string]interface{}{"identifier": "c-001"})
		createClient(t, map[string]interface{}{"identifier": "c-002", "email": "c2@example.com"})
		w := do(http.MethodGet, "/api/clients", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("クライアントが存在しない場合空リストを返す", func(t *testing.T) {
		truncateTables(t)
		w := do(http.MethodGet, "/api/clients", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d", w.Code)
		}
	})
}

func TestClient_Show(t *testing.T) {
	truncateTables(t)

	t.Run("クライアント詳細が取得できる", func(t *testing.T) {
		c := createClient(t, nil)
		w := do(http.MethodGet, fmt.Sprintf("/api/clients/%d", c.ID), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["name"] != c.Name {
			t.Errorf("want name=%s, got %v", c.Name, body["name"])
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/clients/99999", nil)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_Store(t *testing.T) {
	truncateTables(t)

	t.Run("クライアントが登録できる", func(t *testing.T) {
		payload := map[string]string{
			"name":       "新規テスト株式会社",
			"identifier": "new-client-001",
			"post_code":  "100-0001",
			"pref":       "東京都",
			"city":       "千代田区",
			"address":    "千代田1-1",
			"tel":        "0312345678",
			"email":      "new@example.com",
		}
		w := do(http.MethodPost, "/api/clients/store", payload)
		if w.Code != http.StatusCreated {
			t.Errorf("want 201, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["id"] == nil {
			t.Error("id not found in response")
		}
	})

	t.Run("name必須バリデーション", func(t *testing.T) {
		w := do(http.MethodPost, "/api/clients/store", map[string]string{})
		if w.Code != http.StatusBadRequest {
			t.Errorf("want 400, got %d", w.Code)
		}
	})
}

func TestClient_Update(t *testing.T) {
	truncateTables(t)

	t.Run("クライアントが更新できる", func(t *testing.T) {
		c := createClient(t, nil)
		newName := "更新後クライアント名"
		w := do(http.MethodPut, fmt.Sprintf("/api/clients/%d/update", c.ID),
			map[string]string{"name": newName})
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["name"] != newName {
			t.Errorf("want name=%s, got %v", newName, body["name"])
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		w := do(http.MethodPut, "/api/clients/99999/update", map[string]string{"name": "test"})
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_Destroy(t *testing.T) {
	truncateTables(t)

	t.Run("クライアントが削除できる", func(t *testing.T) {
		c := createClient(t, nil)
		w := do(http.MethodDelete, fmt.Sprintf("/api/clients/%d/delete", c.ID), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		truncateTables(t)
		w := do(http.MethodDelete, "/api/clients/99999/delete", nil)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}
