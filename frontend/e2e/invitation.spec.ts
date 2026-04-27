import { test, expect, stubRoute, mockCommon, BACKENDS, type Page } from "./helpers";

// ─── ヘルパー ────────────────────────────────────────────────────────────────

/** 設定メニュー → 招待URL の2段階でモーダルを開く */
async function openInvitationModal(page: Page) {
  await page.getByRole("button", { name: "設定メニューを開く" }).click();
  await page.getByRole("button", { name: "招待URL" }).click();
}

// ─── 招待ランディングページ（バックエンド非依存・静的レンダリング） ──────────────

test.describe("招待ランディングページ", () => {
  test("短いトークンはそのまま表示される", async ({ page }) => {
    await page.goto("/invitation/abc123");
    await expect(page.getByText("abc123")).toBeVisible();
  });

  test("長いトークンは先頭6文字+末尾4文字に省略される", async ({ page }) => {
    // 20文字 > head(6)+tail(4)+3=13 → 省略される
    await page.goto("/invitation/abcdefghijklmnopqrst");
    await expect(page.getByText(/abcdef\.\.\.qrst/)).toBeVisible();
  });

  test("ちょうど13文字のトークンは省略されない", async ({ page }) => {
    // head(6)+tail(4)+3=13 以下 → そのまま表示
    await page.goto("/invitation/abc1234567890");
    await expect(page.getByText("abc1234567890")).toBeVisible();
  });

  test("ログインへリンクが /login を指している", async ({ page }) => {
    await page.goto("/invitation/some-token");
    const link = page.getByRole("link", { name: "ログインへ" });
    await expect(link).toBeVisible();
    await expect(link).toHaveAttribute("href", "/login");
  });
});

// ─── 招待URLモーダル（バックエンド別） ───────────────────────────────────────

for (const backend of BACKENDS) {
  const API = backend.apiPrefix;

  test.describe(`招待URL [${backend.label}]`, () => {
    test.beforeEach(async ({ page, isReal }) => {
      await mockCommon(page, API);
      await stubRoute(page, `${API}/admin/invitation`, {
        found: true,
        url: "http://localhost:3000/invitation/current-token-abc123",
        display_url: "localhost:3000/invitation/current-token-abc123",
        token: "current-token-abc123",
      }, isReal);
      await stubRoute(page, `${API}/clients*`, [], isReal);

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
      await expect(urlField).toHaveValue(/current-token-abc123/);
    });

    // ── 再発行 ───────────────────────────────────────────────────────────────

    test("URLを再発行すると新しいURLが表示される", async ({ page }) => {
      // issue レスポンスは常にモック（特定トークン値をアサートするため）
      await page.route(`${API}/admin/invitation/issue`, (route) =>
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
      await expect(urlField).toHaveValue(/current-token-abc123/);

      await page.getByRole("button", { name: "URLを再発行" }).click();
      await expect(urlField).toHaveValue(/new-token-xyz789/);
    });

    // ── エラー・モック警告 ────────────────────────────────────────────────────

    test("GET /admin/invitation エラー時にモック警告バナーが表示される", async ({ page }) => {
      // エラー UI のテストのため常にモック
      await page.route(`${API}/admin/invitation`, (route) =>
        route.fulfill({ status: 500, json: { message: "internal_server_error" } }),
      );

      await openInvitationModal(page);
      await expect(
        page.getByText(/APIが未接続・エラー時のため、モックの招待URLを表示しています/),
      ).toBeVisible();
    });

    test("再発行エラー時にモック警告バナーが表示される", async ({ page }) => {
      // エラー UI のテストのため常にモック
      await page.route(`${API}/admin/invitation/issue`, (route) =>
        route.fulfill({ status: 500, json: { message: "internal_server_error" } }),
      );

      await openInvitationModal(page);
      await expect(page.locator("#invitation-url-field")).toHaveValue(/current-token-abc123/);

      await page.getByRole("button", { name: "URLを再発行" }).click();
      await expect(
        page.getByText(/APIが未接続・エラー時のため、モックの招待URLを表示しています/),
      ).toBeVisible();
    });

    // ── コピー ───────────────────────────────────────────────────────────────

    test("URLをコピーするとボタンテキストが変わる", async ({ page }) => {
      await page.context().grantPermissions(["clipboard-read", "clipboard-write"]);

      await openInvitationModal(page);
      await expect(page.locator("#invitation-url-field")).toHaveValue(/current-token-abc123/);

      await page.getByRole("button", { name: /クリップボードにコピー/ }).click();
      await expect(page.getByRole("button", { name: /コピーしました/ })).toBeVisible();
    });
  });
}
