import { test, expect } from "@playwright/test";
import { mockCommon, mockClients, mockClientDetail } from "./helpers";

const API = "**/function/php/api";

test.describe("クライアント", () => {
  test.beforeEach(async ({ page }) => {
    await mockCommon(page);
  });

  // -----------------------------------------------------------------------
  // 一覧
  // -----------------------------------------------------------------------
  test.describe("一覧", () => {
    test.beforeEach(async ({ page }) => {
      await page.route(`${API}/clients*`, (route) =>
        route.fulfill({ json: mockClients }),
      );
      await page.goto("/clients");
    });

    test("クライアント一覧が表示される", async ({ page }) => {
      await expect(page.getByText("クライアント一覧")).toBeVisible();
      await expect(page.getByText("株式会社サンプル")).toBeVisible();
      await expect(page.getByText("テスト商事")).toBeVisible();
    });

    test("ステータスバッジが表示される", async ({ page }) => {
      await expect(page.getByText("利用中")).toBeVisible();
      await expect(page.getByText("準備中")).toBeVisible();
    });

    test("会社名で検索できる", async ({ page }) => {
      await page.getByPlaceholder("会社名で検索...").fill("サンプル");

      await expect(page.getByText("株式会社サンプル")).toBeVisible();
      await expect(page.getByText("テスト商事")).not.toBeVisible();
    });

    test("条件クリアで検索が解除される", async ({ page }) => {
      await page.getByPlaceholder("会社名で検索...").fill("サンプル");
      await page.getByText("条件クリア").click();

      await expect(page.getByText("株式会社サンプル")).toBeVisible();
      await expect(page.getByText("テスト商事")).toBeVisible();
    });

    test("新規登録ボタンが表示される", async ({ page }) => {
      await expect(page.getByText("新規登録")).toBeVisible();
    });
  });

  // -----------------------------------------------------------------------
  // 新規登録
  // -----------------------------------------------------------------------
  test.describe("新規登録", () => {
    test.beforeEach(async ({ page }) => {
      await page.goto("/clients/create");
    });

    test("登録フォームが表示される", async ({ page }) => {
      await expect(page.getByText("クライアント登録")).toBeVisible();
    });

    test("必須項目が未入力の場合バリデーションが働く", async ({ page }) => {
      await page.getByRole("button", { name: "登録" }).click();

      // HTML5 バリデーションによりフォームが送信されない（ページ遷移しない）
      await expect(page).toHaveURL("/clients/create");
    });

    test("登録成功で一覧へ遷移する", async ({ page }) => {
      await page.route(`${API}/clients/store`, (route) =>
        route.fulfill({ json: { id: 3 } }),
      );
      await page.route(`${API}/clients*`, (route) =>
        route.fulfill({ json: mockClients }),
      );

      await page.getByPlaceholder("株式会社モックデータ商事").fill("新規テスト株式会社");
      await page.getByPlaceholder("0000000", { exact: true }).fill("1000001");
      await page.getByPlaceholder("架空市中央区みなみ町").fill("千代田区");
      await page.getByPlaceholder("1丁目2番3号").fill("1-1");
      await page.getByPlaceholder("09000000000").fill("0312345678");
      await page.getByPlaceholder("contact@example.com").fill("new@example.com");

      await page.getByRole("button", { name: "登録" }).click();
      await page.getByRole("button", { name: "登録する" }).click();

      await expect(page).toHaveURL("/clients");
    });
  });

  // -----------------------------------------------------------------------
  // 詳細
  // -----------------------------------------------------------------------
  test.describe("詳細", () => {
    test.beforeEach(async ({ page }) => {
      await page.route(`${API}/clients/1`, (route) =>
        route.fulfill({ json: mockClientDetail }),
      );
      await page.goto("/clients/show?id=1");
    });

    test("クライアント詳細が表示される", async ({ page }) => {
      await expect(page.getByText("株式会社サンプル")).toBeVisible();
      await expect(page.getByText("利用中")).toBeVisible();
    });

    test("一覧へ戻るリンクが表示される", async ({ page }) => {
      await expect(page.getByText("一覧へ戻る")).toBeVisible();
    });
  });

  // -----------------------------------------------------------------------
  // 編集
  // -----------------------------------------------------------------------
  test.describe("編集", () => {
    test.beforeEach(async ({ page }) => {
      await page.route(`${API}/clients/1`, (route) =>
        route.fulfill({ json: mockClientDetail }),
      );
      await page.goto("/clients/edit?id=1");
    });

    test("編集フォームに既存データが表示される", async ({ page }) => {
      await expect(
        page.locator('input[placeholder="株式会社モックデータ商事"]'),
      ).toHaveValue("株式会社サンプル");
      await expect(
        page.locator('input[placeholder="contact@example.com"]'),
      ).toHaveValue("sample@example.com");
    });

    test("更新成功で一覧へ遷移する", async ({ page }) => {
      await page.route(`${API}/clients/1/update`, (route) =>
        route.fulfill({ json: mockClientDetail }),
      );
      await page.route(`${API}/clients*`, (route) =>
        route.fulfill({ json: mockClients }),
      );

      await page.getByRole("button", { name: "更新" }).click();
      await page.getByRole("button", { name: "更新する" }).click();

      await expect(page).toHaveURL("/clients");
    });
  });
});
