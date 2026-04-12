import { test, expect } from "@playwright/test";
import { mockCommon, mockClients } from "./helpers";

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

  test("E2E モードでログインするとクライアント一覧へ遷移する", async ({ page }) => {
    await mockCommon(page);
    await page.route("**/function/php/api/clients*", (route) =>
      route.fulfill({ json: mockClients }),
    );

    await page.goto("/login");
    await page.getByText("Googleで続行").click();

    await expect(page).toHaveURL("/clients");
    await expect(page.getByText("クライアント一覧")).toBeVisible();
  });
});
