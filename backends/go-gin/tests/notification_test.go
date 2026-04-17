package tests

import (
	"fmt"
	"net/http"
	"testing"
)

func TestNotification_Counts(t *testing.T) {
	truncateTables(t)

	t.Run("通知件数が取得できる", func(t *testing.T) {
		staff := createStaff(t, nil)
		createNotification(t, staff.ID, "未読通知1")
		n2 := createNotification(t, staff.ID, "未読通知2")
		// 既読にする
		testDB.Model(n2).Update("read", true)

		w := do(http.MethodGet, "/api/notifications/counts", nil,
			withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["unread"] != float64(1) {
			t.Errorf("want unread=1, got %v", body["unread"])
		}
		if body["total"] != float64(2) {
			t.Errorf("want total=2, got %v", body["total"])
		}
	})

	t.Run("未認証で401が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/notifications/counts", nil)
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})
}

func TestNotification_Index(t *testing.T) {
	truncateTables(t)

	t.Run("通知一覧が取得できる", func(t *testing.T) {
		staff := createStaff(t, nil)
		createNotification(t, staff.ID, "通知1")
		createNotification(t, staff.ID, "通知2")

		w := do(http.MethodGet, "/api/notifications", nil,
			withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["items"] == nil {
			t.Error("items not found in response")
		}
	})

	t.Run("url付き通知がレスポンスに含まれる", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "url-notif@example.com"})
		createNotification(t, staff.ID, "クライアント登録", map[string]interface{}{"url": "/clients/show?id=1"})

		w := do(http.MethodGet, "/api/notifications", nil,
			withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		items := body["items"].([]interface{})
		item := items[0].(map[string]interface{})
		if item["url"] != "/clients/show?id=1" {
			t.Errorf("want url=/clients/show?id=1, got %v", item["url"])
		}
	})

	t.Run("未認証で401が返る", func(t *testing.T) {
		w := do(http.MethodGet, "/api/notifications", nil)
		if w.Code != http.StatusUnauthorized {
			t.Errorf("want 401, got %d", w.Code)
		}
	})
}

func TestNotification_Store(t *testing.T) {
	t.Run("通知トリガーが受け付けられる", func(t *testing.T) {
		payload := map[string]string{"title": "新規通知", "body": "通知本文"}
		w := do(http.MethodPost, "/api/notifications", payload)
		if w.Code != http.StatusAccepted {
			t.Errorf("want 202, got %d: %s", w.Code, w.Body.String())
		}
	})
}

func TestNotification_ReadAll(t *testing.T) {
	truncateTables(t)

	t.Run("一括既読が成功する", func(t *testing.T) {
		staff := createStaff(t, nil)
		createNotification(t, staff.ID, "通知A")
		createNotification(t, staff.ID, "通知B")

		w := do(http.MethodPatch, "/api/notifications", nil, withCookie("staff_id", fmt.Sprintf("%d", staff.ID)))
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})
}

func TestNotification_Read(t *testing.T) {
	truncateTables(t)

	t.Run("単一通知が既読になる", func(t *testing.T) {
		staff := createStaff(t, nil)
		n := createNotification(t, staff.ID, "個別通知")

		w := do(http.MethodPatch, fmt.Sprintf("/api/notifications/%d", n.ID), nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})
}
