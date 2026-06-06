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
}

func TestStaff_UpdateRole(t *testing.T) {
	truncateTables(t)

	t.Run("ロールが更新できる", func(t *testing.T) {
		staff := createStaff(t, map[string]interface{}{"email": "target@example.com", "role": 2})
		executor := createStaff(t, map[string]interface{}{"email": "executor@example.com", "role": 1})
		w := do(http.MethodPatch, fmt.Sprintf("/api/staffs/%d/updateRole", staff.ID),
			map[string]interface{}{"role": 1, "version": staff.Version},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("バージョン不一致で409が返る", func(t *testing.T) {
		truncateTables(t)
		staff := createStaff(t, map[string]interface{}{"email": "ver@example.com", "role": 2})
		executor := createStaff(t, map[string]interface{}{"email": "exec_ver@example.com", "role": 1})
		w := do(http.MethodPatch, fmt.Sprintf("/api/staffs/%d/updateRole", staff.ID),
			map[string]interface{}{"role": 1, "version": staff.Version + 99},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusConflict {
			t.Errorf("want 409, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		executor := createStaff(t, map[string]interface{}{"email": "exec2@example.com"})
		w := do(http.MethodPatch, "/api/staffs/99999/updateRole",
			map[string]interface{}{"role": 1, "version": 0},
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
		w := do(http.MethodDelete, fmt.Sprintf("/api/staffs/%d/delete", staff.ID),
			map[string]interface{}{"version": staff.Version},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusOK {
			t.Errorf("want 200, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("バージョン不一致で409が返る", func(t *testing.T) {
		truncateTables(t)
		staff := createStaff(t, map[string]interface{}{"email": "del_ver@example.com"})
		executor := createStaff(t, map[string]interface{}{"email": "exec_del_ver@example.com"})
		w := do(http.MethodDelete, fmt.Sprintf("/api/staffs/%d/delete", staff.ID),
			map[string]interface{}{"version": staff.Version + 99},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusConflict {
			t.Errorf("want 409, got %d: %s", w.Code, w.Body.String())
		}
	})

	t.Run("存在しないIDで404が返る", func(t *testing.T) {
		executor := createStaff(t, map[string]interface{}{"email": "exec3@example.com"})
		w := do(http.MethodDelete, "/api/staffs/99999/delete",
			map[string]interface{}{"version": 0},
			withCookie("staff_id", fmt.Sprintf("%d", executor.ID)),
		)
		if w.Code != http.StatusNotFound {
			t.Errorf("want 404, got %d", w.Code)
		}
	})
}
