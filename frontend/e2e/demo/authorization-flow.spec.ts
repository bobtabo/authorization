import { test, expect, type Locator, type Page } from "@playwright/test";

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
const MAILPIT = "http://localhost:8025";

/** 人間らしい操作に見せるための待機 */
async function humanDelay(ms = 800): Promise<void> {
  await new Promise((r) => setTimeout(r, ms));
}

/** 録画に実カーソルは映らないため、擬似カーソルを描画してクリック操作を可視化する */
async function installCursor(page: Page): Promise<void> {
  await page.addInitScript(() => {
    // addInitScript の実行時点では document.documentElement がまだ存在しないため、
    // DOMContentLoaded まで要素の追加を遅延させる
    function setup(): void {
      const cursor = document.createElement("div");
      Object.assign(cursor.style, {
        position: "fixed",
        top: "0",
        left: "0",
        width: "18px",
        height: "18px",
        borderRadius: "50%",
        background: "rgba(59, 130, 246, 0.9)",
        border: "2px solid white",
        boxShadow: "0 1px 4px rgba(0,0,0,0.4)",
        pointerEvents: "none",
        zIndex: "2147483647",
        transform: "translate(-50%, -50%)",
        transition: "left 0.05s linear, top 0.05s linear, transform 0.1s ease-out",
      });
      document.documentElement.appendChild(cursor);

      const style = document.createElement("style");
      style.textContent = `
        @keyframes demo-cursor-ripple {
          from { opacity: 0.8; transform: translate(-50%, -50%) scale(0.4); }
          to { opacity: 0; transform: translate(-50%, -50%) scale(2.4); }
        }
      `;
      document.documentElement.appendChild(style);

      let currentX = 0;
      let currentY = 0;

      window.addEventListener(
        "mousemove",
        (e) => {
          currentX = e.clientX;
          currentY = e.clientY;
          cursor.style.left = `${e.clientX}px`;
          cursor.style.top = `${e.clientY}px`;
        },
        { capture: true },
      );

      // iframe 内の要素へは実際の mousemove イベントが親ページまで届かない
      // （フレーム境界を越えて伝播しない）ため、JS で直接アニメーションさせて移動する
      function moveTo(targetX: number, targetY: number, durationMs = 400): Promise<void> {
        const startX = currentX;
        const startY = currentY;
        const startTime = performance.now();
        return new Promise((resolve) => {
          function step(now: number): void {
            const t = Math.min(1, (now - startTime) / durationMs);
            const eased = 1 - (1 - t) * (1 - t);
            currentX = startX + (targetX - startX) * eased;
            currentY = startY + (targetY - startY) * eased;
            cursor.style.left = `${currentX}px`;
            cursor.style.top = `${currentY}px`;
            if (t < 1) {
              requestAnimationFrame(step);
            } else {
              resolve();
            }
          }
          requestAnimationFrame(step);
        });
      }
      (window as unknown as { __demoMoveTo: typeof moveTo }).__demoMoveTo = moveTo;

      function ripple(clientX: number, clientY: number): void {
        cursor.style.transform = "translate(-50%, -50%) scale(0.7)";
        const el = document.createElement("div");
        Object.assign(el.style, {
          position: "fixed",
          left: `${clientX}px`,
          top: `${clientY}px`,
          width: "18px",
          height: "18px",
          borderRadius: "50%",
          border: "2px solid rgba(59, 130, 246, 0.8)",
          pointerEvents: "none",
          zIndex: "2147483646",
          animation: "demo-cursor-ripple 0.6s ease-out forwards",
        });
        document.documentElement.appendChild(el);
        el.addEventListener("animationend", () => el.remove());
        setTimeout(() => {
          cursor.style.transform = "translate(-50%, -50%) scale(1)";
        }, 120);
      }
      // 実クリックせずに見た目のリップルだけ出したい場合（新規タブが開くリンク等）に使う
      (window as unknown as { __demoRipple: typeof ripple }).__demoRipple = ripple;

      window.addEventListener(
        "mousedown",
        (e) => ripple(e.clientX, e.clientY),
        { capture: true },
      );
    }

    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", setup);
    } else {
      setup();
    }
  });
}

/** 擬似カーソルを対象要素までなめらかに移動させてからクリックする */
async function moveAndClick(page: Page, locator: Locator): Promise<void> {
  await locator.scrollIntoViewIfNeeded();
  const box = await locator.boundingBox();
  if (!box) throw new Error("要素の座標を取得できませんでした");
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2, { steps: 25 });
  await humanDelay(300);
  await page.mouse.down();
  await humanDelay(100);
  await page.mouse.up();
}

/**
 * 擬似カーソルを対象要素まで移動し、見た目のリップルだけ発火する（実クリックはしない）。
 * target="_blank" のリンク等、実クリックすると新規タブにフォーカスが奪われ、
 * 録画対象ページの動画キャプチャが止まってしまうケースで使う。
 */
async function moveAndFakeClick(page: Page, locator: Locator): Promise<void> {
  await locator.scrollIntoViewIfNeeded();
  const box = await locator.boundingBox();
  if (!box) throw new Error("要素の座標を取得できませんでした");
  const x = box.x + box.width / 2;
  const y = box.y + box.height / 2;
  await page.evaluate(
    ([px, py]) =>
      (window as unknown as { __demoMoveTo: (x: number, y: number) => Promise<void> }).__demoMoveTo(px, py),
    [x, y],
  );
  await humanDelay(300);
  await page.evaluate(
    ([px, py]) =>
      (window as unknown as { __demoRipple: (x: number, y: number) => void }).__demoRipple(px, py),
    [x, y],
  );
}

/**
 * 画面全体を覆うキャプションを一時的に表示する。
 * Slack 等の外部ツールは映せないため、状況説明のテキストで橋渡しする。
 */
async function showCaption(page: Page, lines: string[], displayMs = 1800): Promise<void> {
  await page.evaluate((text) => {
    const overlay = document.createElement("div");
    overlay.id = "__demo_caption__";
    Object.assign(overlay.style, {
      position: "fixed",
      inset: "0",
      background: "rgba(15, 23, 42, 0.94)",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: "12px",
      zIndex: "2147483647",
      color: "#ffffff",
      fontFamily:
        "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Hiragino Sans', sans-serif",
      textAlign: "center",
      padding: "0 48px",
      opacity: "0",
      transition: "opacity 0.35s ease",
    });
    for (const [i, line] of text.entries()) {
      const p = document.createElement("p");
      p.textContent = line;
      Object.assign(p.style, {
        margin: "0",
        fontSize: i === 0 ? "22px" : "16px",
        fontWeight: i === 0 ? "600" : "400",
        color: i === 0 ? "#ffffff" : "#cbd5e1",
        lineHeight: "1.6",
      });
      overlay.appendChild(p);
    }
    document.documentElement.appendChild(overlay);
    requestAnimationFrame(() => {
      overlay.style.opacity = "1";
    });
  }, lines);
  await humanDelay(displayMs);
  await page.evaluate(() => {
    const el = document.getElementById("__demo_caption__");
    if (!el) return;
    el.style.opacity = "0";
    setTimeout(() => el.remove(), 350);
  });
  await humanDelay(450);
}

test("認可フロー全体のデモ録画", async ({ page }) => {
  // 録画中にどのメールを開いたか分かりやすくするため、過去メールを一掃しておく
  await page.request.delete(`${MAILPIT}/api/v1/messages`);

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

  // 録画上でクリック位置が分かるよう、疑似カーソルを描画する
  await installCursor(page);

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
  await moveAndClick(page, page.getByRole("button", { name: "設定メニューを開く" }));
  await humanDelay(500);
  await moveAndClick(page, page.getByRole("button", { name: "招待URL" }));
  await expect(page.getByRole("dialog")).toBeVisible();
  await humanDelay(1500);

  // 招待 URL をコピー
  await page.context().grantPermissions(["clipboard-read", "clipboard-write"]);
  await expect(page.locator("#invitation-url-field")).toHaveValue(/\/invitation\//);
  await moveAndClick(page, page.getByRole("button", { name: /クリップボードにコピー/ }));
  await expect(page.getByRole("button", { name: /コピーしました/ })).toBeVisible();
  await humanDelay(1500);

  // モーダルを閉じる
  await moveAndClick(page, page.getByRole("button", { name: "閉じる" }));
  await expect(page.getByRole("dialog")).not.toBeVisible();
  await humanDelay(500);

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 2. 招待リンクからのログイン
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // コピーした招待URLは Slack 等で共有される想定だが、外部ツールの画面は
  // 映せないため、状況説明のキャプションで橋渡しする
  await showCaption(
    page,
    ["コピーした招待URLを Slack 等で共有", "招待されたスタッフがリンクをクリック"],
    3800,
  );

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
  await moveAndClick(page, page.getByText("Googleで続行"));
  await expect(page).toHaveURL("/clients", { timeout: 10000 });
  await expect(page.getByText("クライアント一覧")).toBeVisible();
  await humanDelay(1500);

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 3. クライアント登録（実バックエンド）
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // clients モックを解除して実バックエンドで登録する（メール送信のため）
  await page.unroute(`${API}/clients*`);

  // 郵便番号 API のモック（外部 API のため維持）
  await page.route("https://apis.postcode-jp.com/**", (route) =>
    route.fulfill({ json: [{ pref: "東京都", city: "渋谷区", town: "" }] }),
  );

  // 新規登録ページへ遷移
  await moveAndClick(page, page.getByText("新規登録"));
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

  // 登録ボタン → 確認ダイアログ → 登録実行
  await moveAndClick(page, page.getByRole("button", { name: "登録" }));
  await humanDelay(500);
  await moveAndClick(page, page.getByRole("button", { name: "登録する" }));

  // 一覧に遷移し、登録したクライアントが表示される
  await expect(page).toHaveURL("/clients", { timeout: 10000 });
  await expect(page.getByText("株式会社デモテスト")).toBeVisible();
  await humanDelay(1500);

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // 4. MailPit でメールを確認 → QR ページ表示
  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  // MailPit API でメール到着を待機し、activate URL を取得
  let qrPath = "";
  for (let i = 0; i < 30; i++) {
    const res = await page.request.get(`${MAILPIT}/api/v1/messages`);
    const json = (await res.json()) as {
      messages: Array<{ ID: string; Subject: string }>;
    };
    const mail = json.messages?.find((m) =>
      m.Subject.includes("ご利用開始"),
    );
    if (mail) {
      const msgRes = await page.request.get(
        `${MAILPIT}/api/v1/message/${mail.ID}`,
      );
      const msg = (await msgRes.json()) as { HTML: string };
      const match = msg.HTML?.match(
        /href="([^"]*\/clients\/[^"]*\/qr)"/,
      );
      if (match) {
        qrPath = new URL(match[1]).pathname;
        break;
      }
    }
    await humanDelay(1000);
  }
  expect(qrPath).toBeTruthy();

  // MailPit 画面に遷移してメールを確認
  await page.goto(MAILPIT);
  await expect(
    page.getByText("ご利用開始のご案内").first(),
  ).toBeVisible({ timeout: 5000 });
  await humanDelay(1000);
  await moveAndClick(page, page.getByText("ご利用開始のご案内").first());
  await humanDelay(2000);

  // メール本文（iframe）内の「ご利用を開始する」ボタンにカーソルを当てる
  // （リンクは target="_blank" のため、実クリックすると新規タブにフォーカスが奪われ
  //   録画対象ページの動画キャプチャが止まってしまう。見た目のリップルのみ発火する）
  const mailFrame = page.frameLocator("iframe#preview-html");
  const activateLink = mailFrame.getByRole("link", { name: "ご利用を開始する" });
  await moveAndFakeClick(page, activateLink);
  await humanDelay(1500);

  // QR ページの API をモック（メール取得までは実バックエンドが必須だが、QR 表示自体は不要）
  const qrIdentifier = qrPath.split("/").at(-2) ?? "";
  await page.route(`${API}/clients/${qrIdentifier}/qr`, (route) =>
    route.fulfill({
      json: {
        message: "SUCCESS",
        identifier: qrIdentifier,
        deeplink_url: `authgateway://clients/${qrIdentifier}/info`,
        version: null,
      },
    }),
  );

  // メール内の activate URL から QR ページへ遷移
  await page.goto(qrPath);
  await expect(page.getByText("スマホアプリ連携")).toBeVisible({
    timeout: 10000,
  });
  await expect(
    page.getByText("スマホアプリでQRコードを読み取ってください"),
  ).toBeVisible();
  // ヘッダーのロゴアイコンも svg のため、"svg" だけでは常にマッチしてしまい
  // QR コード自体の描画完了を待てない。読み込み中表示が消えるのを待ってから
  // main 内の svg（QRコード本体）を確認する。
  await expect(page.getByText("読み込み中...")).not.toBeVisible({
    timeout: 10000,
  });
  await expect(page.locator("main svg")).toBeVisible({ timeout: 10000 });
  await humanDelay(2000);
});
