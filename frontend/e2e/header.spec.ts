import { test, expect } from "@playwright/test";
import { mockCommon, mockLogout, mockClients, mockClientDetail, BACKENDS } from "./helpers";

const PHP_API = "**/function/php/api";

const PHP_NOTIF_API = "**/function/php/api";

// ---------------------------------------------------------------------------
// 通知: url付き通知クリックで遷移
// ---------------------------------------------------------------------------
test.describe("通知 / URLリンク遷移", () => {
  test.beforeEach(async ({ page }) => {
    await mockCommon(page, PHP_NOTIF_API);

    // url付き通知をモック
    await page.route(`${PHP_NOTIF_API}/notifications/counts`, (route) =>
      route.fulfill({ json: { unread: 1, total: 1 } }),
    );
    await page.route(`${PHP_NOTIF_API}/notifications*`, (route) =>
      route.fulfill({
        json: {
          items: [
            {
              id: 1,
              title: "新しいクライアントが登録されました",
              message: "株式会社サンプル",
              message_type: 1,
              url: "/clients/show?id=1",
              read: false,
            },
          ],
          next_cursor: null,
        },
      }),
    );
    await page.route(`${PHP_NOTIF_API}/clients/1`, (route) =>
      route.fulfill({ json: mockClientDetail }),
    );
    await page.route(`${PHP_NOTIF_API}/clients*`, (route) =>
      route.fulfill({ json: mockClients }),
    );
    await page.goto("/clients");
  });

  test("url付き通知をクリックすると詳細ページへ遷移する", async ({ page }) => {
    await page.getByRole("button", { name: /通知/ }).click();
    await expect(page.getByText("新しいクライアントが登録されました")).toBeVisible();
    await page.getByText("新しいクライアントが登録されました").click();
    await expect(page).toHaveURL(/clients\/show\?id=1/);
  });
});

// ---------------------------------------------------------------------------
// バックエンドランタイム切り替え
// ---------------------------------------------------------------------------
test.describe("ヘッダー / バックエンド切り替え", () => {
  test.beforeEach(async ({ page }) => {
    await mockCommon(page, PHP_API);
    await page.route(`${PHP_API}/clients*`, (route) =>
      route.fulfill({ json: mockClients }),
    );
    await page.goto("/clients");
  });

  test("デフォルトでPHPが選択されている", async ({ page }) => {
    await expect(page.getByLabel("Backend:")).toHaveValue("php");
  });

  test("ランタイムを切り替えるとログアウトしてログインへ遷移する", async ({ page }) => {
    await mockLogout(page, PHP_API);

    await page.getByLabel("Backend:").selectOption("go");

    await expect(page).toHaveURL("/login");
  });

  test("切り替え後にログインページが表示される", async ({ page }) => {
    await mockLogout(page, PHP_API);

    await page.getByLabel("Backend:").selectOption("go");
    await expect(page.getByText("Googleで続行")).toBeVisible();
  });
});

// ---------------------------------------------------------------------------
// 全バックエンド共通: auth/me が正常に返る
// 実装完了したバックエンドを BACKENDS に追加することで自動的にテスト対象になります。
// ---------------------------------------------------------------------------
for (const backend of BACKENDS) {
  test.describe(`バックエンド: ${backend.label}`, () => {
    test.beforeEach(async ({ page }) => {
      // 対象バックエンドの API プレフィックスでモックを設定
      await page.route(`${backend.apiPrefix}/auth/me`, (route) =>
        route.fulfill({
          json: { staff_id: 1, name: "テストスタッフ", avatar: null, role: 1 },
        }),
      );
      await page.route(`${backend.apiPrefix}/notifications/counts`, (route) =>
        route.fulfill({ json: { unread: 0, total: 0 } }),
      );
      await page.route(`${backend.apiPrefix}/notifications*`, (route) =>
        route.fulfill({ json: [] }),
      );
      await page.route(`${backend.apiPrefix}/clients*`, (route) =>
        route.fulfill({ json: [] }),
      );

      // localStorage でランタイムを指定してからページロード
      await page.addInitScript((runtime) => {
        localStorage.setItem("backend-runtime", runtime);
      }, backend.value);

      await page.goto("/clients");
    });

    test("ヘッダーに正しいランタイムが表示される", async ({ page }) => {
      await expect(page.getByLabel("Backend:")).toHaveValue(backend.value);
    });

    test("auth/me が返るとログイン済みユーザーが表示される", async ({ page }) => {
      await expect(page.getByText("テストスタッフ")).toBeVisible();
    });
  });
}
