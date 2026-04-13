package tests

import (
	"fmt"
	"net/http"
	"testing"
)

func TestStaff_Index(t *testing.T) {
	truncateTables(t)

	t.Run("スタッフ一覧が取得できる", func(t *testing.T) {
		createStaff(t, map[string]interface{}{"email": "s1@example.com"})
		createStaff(t, map[string]interface{}{"email": "s2@example.com", "name": "別スタッフ"})
		w := do(http.MethodGet, "/api/staffs", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		if body["items"] == nil {
			t.Error("items not found in response")
		}
	})

	t.Run("スタッフが存在しない場合空リストを返す", func(t *testing.T) {
		truncateTables(t)
		w := do(http.MethodGet, "/api/staffs", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d", w.Code)
		}
		body := parseBody(w)
		items, ok := body["items"].([]interface{})
		if !ok || len(items) != 0 {
			t.Errorf("want empty items, got %v", body["items"])
		}
	})
}

func TestStaff_UpdateRole(t *testing.T) {
	truncateTables(t)

	t.Run("ロールが更新できる", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "target@example.com", "role": 2})
		executor := createStaff(t, map[string]interface{}{"email": "executor@example.com", "role": 1})
		w := do(http.MethodPatch, fmt.Sprintf("/api/staffs/%d/updateRole", staff.ID),
			map[string]int{"role": 1},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		executor := createStaff(t, map[string]interface{}{"email": "exec2@example.com"})
		w := do(http.MethodPatch, "/api/staffs/99999/updateRole",
			map[string]int{"role": 1},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}

func TestStaff_Destroy(t *testing.T) {
	truncateTables(t)

	t.Run("スタッフが削除できる", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "del@example.com"})
		executor := createStaff(t, map[string]interface{}{"email": "exec@example.com"})
		w := do(http.MethodDelete, fmt.Sprintf("/api/staffs/%d/delete", staff.ID), nil,
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		executor := createStaff(t, map[string]interface{}{"email": "exec3@example.com"})
		w := do(http.MethodDelete, "/api/staffs/99999/delete", nil,
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}
