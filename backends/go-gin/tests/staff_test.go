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
		if body["data"] == nil {
			t.Error("data not found in response")
		}
		if body["pager"] == nil {
			t.Error("pager not found in response")
		}
	})

	t.Run("スタッフが存在しない場合空リストを返す", func(t *testing.T) {
		truncateTables(t)
		w := do(http.MethodGet, "/api/staffs", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d", w.Code)
		}
		body := parseBody(w)
		data, ok := body["data"].([]interface{})
		if !ok || len(data) != 0 {
			t.Errorf("want empty data, got %v", body["data"])
		}
	})

	t.Run("keywordの_はワイルドカードとして解釈されない", func(t *testing.T) {
		truncateTables(t)
		createStaff(t, map[string]interface{}{"email": "a_b@example.com", "name": "アンダースコア"})
		createStaff(t, map[string]interface{}{"email": "axb@example.com", "name": "エックス"})

		w := do(http.MethodGet, "/api/staffs?keyword=a_b", nil)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
		body := parseBody(w)
		data, _ := body["data"].([]interface{})
		if len(data) != 1 {
			t.Errorf("want 1 result (literal \"a_b\" match only), got %d: %s", len(data), w.Body.String())
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
