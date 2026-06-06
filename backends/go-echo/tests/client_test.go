package tests

import (
	"authorization-go-echo/ent/notification"
	"context"
	"encoding/json"
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
			"name":      "新規テスト株式会社",
			"post_code": "100-0001",
			"pref":      "東京都",
			"city":      "千代田区",
			"address":   "千代田1-1",
			"tel":       "0312345678",
			"email":     "new@example.com",
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

	t.Run("登録時にURL付き通知が作成される", func(t *testing.T) {
		truncateTables(t)
		staff := createStaff(t, nil)
		payload := map[string]string{
			"name":      "通知テスト株式会社",
			"post_code": "100-0001",
			"pref":      "東京都",
			"city":      "千代田区",
			"address":   "千代田1-1",
			"tel":       "0312345678",
			"email":     "notif@example.com",
		}
		w := do(http.MethodPost, "/api/clients/store", payload, withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusCreated {
			t.Errorf("want 201, got %d: %s", w.Code, w.Body.String())
		}
		clientBody := parseBody(w)
		clientID := clientBody["id"].(float64)

		notif, err := testDB.Notification.Query().
			Where(notification.StaffIDEQ(staff.ID)).
			First(context.Background())
		if err != nil {
			t.Fatalf("notification not found: %v", err)
		}
		want := fmt.Sprintf("/clients/show?id=%d", int(clientID))
		if notif.URL == nil || *notif.URL != want {
			t.Errorf("want url=%s, got %v", want, notif.URL)
		}
	})

	t.Run("name必須バリデーション", func(t *testing.T) {
		w := do(http.MethodPost, "/api/clients/store", map[string]string{})
		if w.Code != http.StatusUnprocessableEntity {
			t.Errorf("want 422, got %d", w.Code)
		}
	})
}

func TestClient_Update(t *testing.T) {
	truncateTables(t)

	t.Run("クライアントが更新できる", func(t *testing.T) {
		c := createClient(t, nil)
		newName := "更新後クライアント名"
		w := do(http.MethodPut, fmt.Sprintf("/api/clients/%d/update", c.ID),
			map[string]interface{}{"name": newName, "version": c.Version})
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["name"] != newName {
			t.Errorf("want name=%s, got %v", newName, body["name"])
		}
	})

	t.Run("バージョン不一致で409が返る", func(t *testing.T) {
		truncateTables(t)
		c := createClient(t, nil)
		w := do(http.MethodPut, fmt.Sprintf("/api/clients/%d/update", c.ID),
			map[string]interface{}{"name": "競合テスト", "version": c.Version + 99})
		if w.Code != http.StatusConflict {
			t.Errorf("want 409, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		w := do(http.MethodPut, "/api/clients/99999/update", map[string]interface{}{"name": "test", "version": 0})
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_Destroy(t *testing.T) {
	truncateTables(t)

	t.Run("クライアントが削除できる", func(t *testing.T) {
		c := createClient(t, nil)
		w := do(http.MethodDelete, fmt.Sprintf("/api/clients/%d/delete", c.ID),
			map[string]interface{}{"version": c.Version})
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("バージョン不一致で409が返る", func(t *testing.T) {
		truncateTables(t)
		c := createClient(t, nil)
		w := do(http.MethodDelete, fmt.Sprintf("/api/clients/%d/delete", c.ID),
			map[string]interface{}{"version": c.Version + 99})
		if w.Code != http.StatusConflict {
			t.Errorf("want 409, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		truncateTables(t)
		w := do(http.MethodDelete, "/api/clients/99999/delete",
			map[string]interface{}{"version": 0})
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_GetQr(t *testing.T) {
	truncateTables(t)

	t.Run("QRコードデータが取得できる", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "qr-test-001"})
		w := do(http.MethodGet, fmt.Sprintf("/api/clients/%s/qr", c.Identifier), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["identifier"] != c.Identifier {
			t.Errorf("want identifier=%s, got %v", c.Identifier, body["identifier"])
		}
		wantURL := "authgateway://clients/" + c.Identifier + "/info"
		if body["deeplink_url"] != wantURL {
			t.Errorf("want deeplink_url=%s, got %v", wantURL, body["deeplink_url"])
		}
	})

	t.Run("存在しないidentifierで404が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/clients/no-such-identifier/qr", nil)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_GetInfo(t *testing.T) {
	truncateTables(t)

	t.Run("クライアント情報が取得できる", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "info-test-001", "name": "情報テスト株式会社"})
		w := do(http.MethodGet, fmt.Sprintf("/api/clients/%s/info", c.Identifier), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["identifier"] != c.Identifier {
			t.Errorf("want identifier=%s, got %v", c.Identifier, body["identifier"])
		}
		if body["name"] != c.Name {
			t.Errorf("want name=%s, got %v", c.Name, body["name"])
		}
		if body["status"] == nil {
			t.Error("status not found in response")
		}
	})

	t.Run("存在しないidentifierで404が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/clients/no-such-identifier/info", nil)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_Start(t *testing.T) {
	truncateTables(t)

	t.Run("Inactive状態からActiveに遷移してアクセストークンが返る", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "start-test-001", "status": 1})
		w := do(http.MethodPatch, fmt.Sprintf("/api/clients/%s/start", c.Identifier), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["access_token"] == nil || body["access_token"] == "" {
			t.Error("access_token not found or empty in response")
		}
	})

	t.Run("Active状態でもアクセストークンが返る", func(t *testing.T) {
		truncateTables(t)
		c := createClient(t, map[string]interface{}{"identifier": "start-test-002", "status": 2})
		w := do(http.MethodPatch, fmt.Sprintf("/api/clients/%s/start", c.Identifier), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["access_token"] == nil || body["access_token"] == "" {
			t.Error("access_token not found or empty in response")
		}
	})

	t.Run("存在しないidentifierで404が返る", func(t *testing.T) {
		w := do(http.MethodPatch, "/api/clients/no-such-identifier/start", nil)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_Stop(t *testing.T) {
	truncateTables(t)

	t.Run("Active状態からSuspendedに遷移する", func(t *testing.T) {
		c := createClient(t, map[string]interface{}{"identifier": "stop-test-001", "status": 2})
		w := do(http.MethodPatch, fmt.Sprintf("/api/clients/%s/stop", c.Identifier), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("Active以外の状態でも200が返る", func(t *testing.T) {
		truncateTables(t)
		c := createClient(t, map[string]interface{}{"identifier": "stop-test-002", "status": 1})
		w := do(http.MethodPatch, fmt.Sprintf("/api/clients/%s/stop", c.Identifier), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないidentifierで404が返る", func(t *testing.T) {
		w := do(http.MethodPatch, "/api/clients/no-such-identifier/stop", nil)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestClient_SoftDelete(t *testing.T) {
	truncateTables(t)

	t.Run("論理削除済みのクライアントが一覧に含まれる", func(t *testing.T) {
		c := createClient(t, nil)
		do(http.MethodDelete, fmt.Sprintf("/api/clients/%d/delete", c.ID),
			map[string]interface{}{"version": c.Version})
		w := do(http.MethodGet, "/api/clients", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d", w.Code)
		}
		var list []map[string]interface{}
		json.Unmarshal(w.Body.Bytes(), &list)
		if len(list) != 1 {
			t.Errorf("want 1 item (including soft-deleted), got %d", len(list))
		}
	})

	t.Run("論理削除済みのクライアント詳細が取得できる", func(t *testing.T) {
		truncateTables(t)
		c := createClient(t, nil)
		do(http.MethodDelete, fmt.Sprintf("/api/clients/%d/delete", c.ID),
			map[string]interface{}{"version": c.Version})
		w := do(http.MethodGet, fmt.Sprintf("/api/clients/%d", c.ID), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200 for soft-deleted client, got %d: %s", w.Code, w.Body.String())
		}
	})
}
