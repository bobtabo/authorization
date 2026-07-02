import { test, expect } from "@playwright/test";

/**
 * デモ用シナリオ: 認可サーバーの主要フローを録画し GIF 化する。
 *
 * 前提:
 *   - バックエンドコンテナ（PHP）が起動済みであること
 *   - seed データが投入済みであること（php artisan migrate --seed）
 *   - MailPit が起動済みであること（localhost:8025）
 *
 * 実行:
 *   npx playwright test --project=demo
 */

const API = "**/function/php/api";

/** 人間らしい操作に見せるための待機 */
async function humanDelay(ms = 800): Promise<void> {
  await new Promise((r) => setTimeout(r, ms));
}

test("認可フロー全体のデモ録画", async ({ page }) => {
  // ── 共通モック（auth/me, notifications）──────────────────────────
  await page.route(`${API}/auth/me`, (route) =>
    route.fulfill({
      json: { staff_id: 1, name: "デモ管理者", avatar: null, role: 1 },
    }),
  );
  await page.route(`${API}/notifications/counts`, (route) =>
    route.fulfill({ json: { unread: 0, total: 0 } }),
  );
  await page.route(`${API}/notifications*`, (route) =>
    route.fulfill({ json: [] }),
  );

  // backend-runtime を php に固定
  await page.addInitScript(() => {
    localStorage.setItem("backend-runtime", "php");
  });

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 1. 招待 URL の発行
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // クライアント一覧をモック（ログイン後のランディング用）
  await page.route(`${API}/clients*`, (route) =>
    route.fulfill({
      json: {
        data: [],
        pager: {
          count: 0, limit: 10, next: false, previous: false,
          page: 1, nextPage: 1, previousPage: 1, pageCount: 0,
          first: true, last: true, firstRecordCount: 0, lastRecordCount: 0,
          startPage: 1, endPage: 1,
        },
      },
    }),
  );

  // 招待 URL API をモック
  const invitationToken = "demo-invitation-token-001";
  const invitationUrl = `http://localhost:3000/invitation/${invitationToken}`;
  await page.route(`${API}/admin/invitation*`, (route) =>
    route.fulfill({
      json: {
        found: true,
        url: invitationUrl,
        display_url: `localhost:3000/invitation/${invitationToken}`,
        token: invitationToken,
      },
    }),
  );

  // ログイン済み状態でクライアント一覧にアクセス
  await page.goto("/clients");
  await expect(page.getByText("クライアント一覧")).toBeVisible();
  await humanDelay(1000);

  // 設定メニュー → 招待URL をクリック
  await page.getByRole("button", { name: "設定メニューを開く" }).click();
  await humanDelay(500);
  await page.getByRole("button", { name: "招待URL" }).click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await humanDelay(1500);

  // 招待 URL をコピー
  await page.context().grantPermissions(["clipboard-read", "clipboard-write"]);
  await expect(page.locator("#invitation-url-field")).toHaveValue(/\/invitation\//);
  await page.getByRole("button", { name: /クリップボードにコピー/ }).click();
  await expect(page.getByRole("button", { name: /コピーしました/ })).toBeVisible();
  await humanDelay(1500);

  // モーダルを閉じる
  await page.getByRole("button", { name: "閉じる" }).click();
  await expect(page.getByRole("dialog")).not.toBeVisible();
  await humanDelay(500);

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 2. 招待リンクからのログイン
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 招待トークン検証のモック
  await page.route(`**/auth/invitation/${invitationToken}`, (route) =>
    route.fulfill({ json: { found: true, token: invitationToken } }),
  );

  // auth/me を未認証に切り替え（UserProvider がキャッシュを再設定するのを防ぐ）
  // unroute → route で既存ハンドラを確実に置換する
  await page.unroute(`${API}/auth/me`);
  await page.route(`${API}/auth/me`, (route) =>
    route.fulfill({ status: 401, json: { message: "Unauthenticated." } }),
  );

  // ユーザーキャッシュをクリアしてログイン画面に遷移させる
  await page.evaluate(() => {
    localStorage.removeItem("cachedUser");
  });

  await page.goto(`/invitation/${invitationToken}`);
  await expect(page).toHaveURL(/\/login\?token=/, { timeout: 10000 });
  await humanDelay(1000);

  // auth/me をログイン済みに戻してからログイン操作
  await page.unroute(`${API}/auth/me`);
  await page.route(`${API}/auth/me`, (route) =>
    route.fulfill({
      json: { staff_id: 1, name: "デモ管理者", avatar: null, role: 1 },
    }),
  );
  await page.getByText("Googleで続行").click();
  await expect(page).toHaveURL("/clients", { timeout: 10000 });
  await expect(page.getByText("クライアント一覧")).toBeVisible();
  await humanDelay(1500);

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 3. クライアント登録
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 郵便番号 API のモック
  await page.route("https://apis.postcode-jp.com/**", (route) =>
    route.fulfill({ json: [{ pref: "東京都", city: "渋谷区", town: "" }] }),
  );

  // 登録成功のモック
  const newClientId = 10;
  const clientIdentifier = "demo-client-abc123";
  await page.route(`${API}/clients/store`, (route) =>
    route.fulfill({ json: { id: newClientId } }),
  );

  // クライアント詳細モック（ステップ4 の QR 遷移で identifier を利用）
  const clientDetailMock = {
    id: newClientId,
    name: "株式会社デモテスト",
    identifier: clientIdentifier,
    post_code: "1500001",
    pref: "東京都",
    city: "渋谷区",
    address: "神南1-2-3",
    building: "",
    tel: "0312345678",
    email: "demo@example.com",
    status: 1,
    start_at: "2026-07-01 09:00:00",
    stop_at: null,
    created_at: "2026-07-01 09:00:00",
    updated_at: "2026-07-01 09:00:00",
    version: 1,
  };

  // 登録後の一覧表示用モック（新規クライアントを含む）
  const registeredClients = {
    data: [
      {
        id: newClientId,
        name: "株式会社デモテスト",
        status: 1,
        start_at: "2026-07-01 09:00:00",
        stop_at: null,
        created_at: "2026-07-01 09:00:00",
        updated_at: "2026-07-01 09:00:00",
      },
    ],
    pager: {
      count: 1, limit: 10, next: false, previous: false,
      page: 1, nextPage: 1, previousPage: 1, pageCount: 1,
      first: true, last: true, firstRecordCount: 1, lastRecordCount: 1,
      startPage: 1, endPage: 1,
    },
  };

  // 新規登録ページへ遷移
  await page.getByText("新規登録").click();
  await expect(page.getByText("クライアント登録")).toBeVisible();
  await humanDelay(500);

  // フォーム入力
  await page.getByPlaceholder("株式会社モックデータ商事").fill("株式会社デモテスト");
  await humanDelay(300);
  await page.getByPlaceholder("0000000", { exact: true }).fill("1500001");
  await expect(page.locator('input[placeholder="郵便番号で自動入力"]')).not.toHaveValue("", { timeout: 3000 });
  await humanDelay(300);
  await page.getByPlaceholder("架空市中央区みなみ町").fill("渋谷区");
  await humanDelay(300);
  await page.getByPlaceholder("1丁目2番3号").fill("神南1-2-3");
  await humanDelay(300);
  await page.getByPlaceholder("09000000000").fill("0312345678");
  await humanDelay(300);
  await page.getByPlaceholder("contact@example.com").fill("demo@example.com");
  await humanDelay(800);

  // 一覧を更新するためにモックを差し替え
  await page.route(`${API}/clients*`, (route) =>
    route.fulfill({ json: registeredClients }),
  );

  // 登録ボタン → 確認ダイアログ → 登録実行
  await page.getByRole("button", { name: "登録" }).click();
  await humanDelay(500);
  await page.getByRole("button", { name: "登録する" }).click();

  // 一覧に遷移し、登録したクライアントが表示される
  await expect(page).toHaveURL("/clients", { timeout: 10000 });
  await expect(page.getByText("株式会社デモテスト")).toBeVisible();
  await humanDelay(1500);

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 4. QR ページ表示
  //    実運用ではクライアント登録時に送信されるメール（MailPit で確認）の
  //    URL から遷移するが、デモでは詳細画面で identifier を確認後 QR へ遷移
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // クライアント詳細・JWT 履歴のモック
  await page.route(`${API}/clients/${newClientId}`, (route) =>
    route.fulfill({ json: clientDetailMock }),
  );
  await page.route(`${API}/clients/${newClientId}/jwt-histories*`, (route) =>
    route.fulfill({
      json: {
        data: [],
        pager: {
          count: 0, limit: 10, next: false, previous: false,
          page: 1, nextPage: 1, previousPage: 1, pageCount: 0,
          first: true, last: true, firstRecordCount: 0, lastRecordCount: 0,
          startPage: 1, endPage: 1,
        },
      },
    }),
  );

  // 一覧から詳細画面へ遷移（登録したクライアントをクリック）
  await page.getByText("株式会社デモテスト").click();
  await expect(page.getByText("クライアント詳細")).toBeVisible();
  await expect(page.getByText(clientIdentifier)).toBeVisible();
  await humanDelay(1500);

  // QR コード API のモック
  await page.route(`${API}/clients/${clientIdentifier}/qr`, (route) =>
    route.fulfill({
      json: {
        identifier: clientIdentifier,
        deeplink_url: `authgateway://connect?id=${clientIdentifier}`,
      },
    }),
  );

  // 詳細画面の identifier を確認後、QR ページへ遷移
  // （実運用ではメール内リンクからアクセスする）
  await page.goto(`/clients/${clientIdentifier}/qr`);
  await expect(page.getByText("スマホアプリ連携")).toBeVisible();
  await expect(page.getByText("スマホアプリでQRコードを読み取ってください")).toBeVisible();
  await humanDelay(2000);
});
