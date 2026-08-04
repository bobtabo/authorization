import { test, expect, stubRoute, mockCommon, mockClients, BACKENDS } from "./helpers";

test.describe("認証", () => {
  test("ルートにアクセスするとログインページへリダイレクトされる", async ({ page }) => {
    await page.goto("/");
    await expect(page).toHaveURL("/login");
  });

  test("ログインページが表示される", async ({ page }) => {
    await page.goto("/login");

    await expect(page.getByText("Authorization Gateway", { exact: true })).toBeVisible();
    await expect(page.getByText("Googleで続行")).toBeVisible();
  });

  test("新規登録ページは招待制の案内とログインへのリンクを表示する", async ({ page }) => {
    await page.goto("/register");

    await expect(page.getByRole("heading")).toContainText("新規登録");
    await expect(page.getByText("新規登録は招待制です")).toBeVisible();
    await expect(page.getByRole("link", { name: "ログイン" })).toHaveAttribute("href", "/login");
  });
});

for (const backend of BACKENDS) {
  test.describe(`認証 [${backend.label}]`, () => {
    test("E2E モードでログインするとクライアント一覧へ遷移する", async ({ page, isReal }) => {
      await mockCommon(page, backend.apiPrefix, isReal);
      await stubRoute(page, `${backend.apiPrefix}/clients*`, mockClients, isReal);
      await page.addInitScript((runtime) => {
        localStorage.setItem("backend-runtime", runtime);
      }, backend.value);

      await page.goto("/login");
      await page.getByText("Googleで続行").click();

      await expect(page).toHaveURL("/clients");
      await expect(page.getByText("クライアント一覧")).toBeVisible();
    });
  });
}

for (const backend of BACKENDS) {
  test.describe(`バックエンド: ${backend.label}`, () => {
    test.beforeEach(async ({ page }) => {
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
