import { test, expect, stubRoute, mockCommon, BACKENDS, type Page } from "./helpers";

// ─── ヘルパー ────────────────────────────────────────────────────────────────

/** 設定メニュー → 招待URL の2段階でモーダルを開く */
async function openInvitationModal(page: Page) {
  await page.getByRole("button", { name: "設定メニューを開く" }).click();
  await page.getByRole("button", { name: "招待URL" }).click();
}

// ─── 招待ランディングページ（バックエンド非依存） ────────────────────────────

test.describe("招待ランディングページ", () => {
  test("招待リンクを踏むとトークン付きログインページへリダイレクトされる", async ({ page }) => {
    await page.route("**/auth/invitation/some-token", (route) =>
      route.fulfill({ json: { found: true, token: "some-token" } }),
    );
    await page.goto("/invitation/some-token");
    await expect(page).toHaveURL(/\/login\?token=some-token/, { timeout: 5000 });
  });
});

// ─── 招待URLモーダル（バックエンド別） ───────────────────────────────────────

for (const backend of BACKENDS) {
  const API = backend.apiPrefix;

  test.describe(`招待URL [${backend.label}]`, () => {
    test.beforeEach(async ({ page, isReal }) => {
      await mockCommon(page, API, isReal);
      await stubRoute(page, `${API}/admin/invitation*`, {
        found: true,
        url: "http://localhost:3000/invitation/e2e-tkn-001",
        display_url: "localhost:3000/invitation/e2e-tkn-001",
        token: "e2e-tkn-001",
      }, isReal);
      await stubRoute(page, `${API}/clients*`, { data: [], pager: { count: 0, limit: 10, next: false, previous: false, page: 1, nextPage: 1, previousPage: 1, pageCount: 0, first: true, last: true, firstRecordCount: 0, lastRecordCount: 0, startPage: 1, endPage: 1 } }, false);

      await page.addInitScript((runtime) => {
        localStorage.setItem("backend-runtime", runtime);
      }, backend.value);

      await page.goto("/clients");
    });

    // ── 基本開閉 ────────────────────────────────────────────────────────────

    test("設定メニューから招待URLをクリックするとモーダルが開く", async ({ page }) => {
      await openInvitationModal(page);
      await expect(page.getByRole("dialog")).toBeVisible();
      await expect(page.getByText("招待URL").first()).toBeVisible();
    });

    test("×ボタンでモーダルが閉じる", async ({ page }) => {
      await openInvitationModal(page);
      await expect(page.getByRole("dialog")).toBeVisible();

      await page.getByRole("button", { name: "閉じる" }).click();
      await expect(page.getByRole("dialog")).not.toBeVisible();
    });

    test("Escapeキーでモーダルが閉じる", async ({ page }) => {
      await openInvitationModal(page);
      await expect(page.getByRole("dialog")).toBeVisible();

      await page.keyboard.press("Escape");
      await expect(page.getByRole("dialog")).not.toBeVisible();
    });

    test("背景（オーバーレイ）クリックでモーダルが閉じる", async ({ page }) => {
      await openInvitationModal(page);
      await expect(page.getByRole("dialog")).toBeVisible();

      // backdrop は role="presentation"。左上隅はダイアログの外
      await page.locator('[role="presentation"]').click({ position: { x: 10, y: 10 } });
      await expect(page.getByRole("dialog")).not.toBeVisible();
    });

    // ── URL表示 ──────────────────────────────────────────────────────────────

    test("モーダルに現在の招待URLが表示される", async ({ page }) => {
      await openInvitationModal(page);
      const urlField = page.locator("#invitation-url-field");
      await expect(urlField).toBeVisible();
      await expect(urlField).toHaveValue(/\/invitation\//, { timeout: 15000 });
    });

    // ── 再発行 ───────────────────────────────────────────────────────────────

    test("URLを再発行すると新しいURLが表示される", async ({ page }) => {
      // issue レスポンスは常にモック（特定トークン値をアサートするため）
      await page.route(`${API}/admin/invitation/issue*`, (route) =>
        route.fulfill({
          json: {
            found: true,
            url: "http://localhost:3000/invitation/new-token-xyz789",
            display_url: "localhost:3000/invitation/new-token-xyz789",
            token: "new-token-xyz789",
          },
        }),
      );

      await openInvitationModal(page);
      const urlField = page.locator("#invitation-url-field");
      await expect(urlField).toHaveValue(/\/invitation\//, { timeout: 15000 });

      await page.getByRole("button", { name: "URLを再発行" }).click();
      await expect(urlField).toHaveValue(/new-token-xyz789/);
    });

    // ── エラー・モック警告 ────────────────────────────────────────────────────

    test("GET /admin/invitation エラー時にモック警告バナーが表示される", async ({ page }) => {
      // エラー UI のテストのため常にモック（abort で APIが未接続を模擬）
      await page.route(`${API}/admin/invitation*`, (route) => route.abort());

      await openInvitationModal(page);
      await expect(
        page.getByText(/APIが未接続・エラー時のため、モックの招待URLを表示しています/),
      ).toBeVisible();
    });

    test("再発行エラー時にモック警告バナーが表示される", async ({ page }) => {
      // エラー UI のテストのため常にモック（abort で APIが未接続を模擬）
      await page.route(`${API}/admin/invitation/issue*`, (route) => route.abort());

      await openInvitationModal(page);
      await expect(page.locator("#invitation-url-field")).toHaveValue(/\/invitation\//, { timeout: 15000 });

      await page.getByRole("button", { name: "URLを再発行" }).click();
      await expect(
        page.getByText(/APIが未接続・エラー時のため、モックの招待URLを表示しています/),
      ).toBeVisible();
    });

    // ── コピー ───────────────────────────────────────────────────────────────

    test("URLをコピーするとボタンテキストが変わる", async ({ page }) => {
      await page.context().grantPermissions(["clipboard-read", "clipboard-write"]);

      await openInvitationModal(page);
      await expect(page.locator("#invitation-url-field")).toHaveValue(/\/invitation\//, { timeout: 15000 });

      await page.getByRole("button", { name: /クリップボードにコピー/ }).click();
      await expect(page.getByRole("button", { name: /コピーしました/ })).toBeVisible();
    });
  });
}
