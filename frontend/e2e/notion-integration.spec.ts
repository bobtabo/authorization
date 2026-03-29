import { expect, test } from "@playwright/test";

test.describe("Notion 連携", () => {
  test("ログイン後、設定から Notion 連携ページを開ける", async ({ page }) => {
    await page.goto("/login");

    await page.getByRole("button", { name: "Googleで続行" }).click();
    await expect(page).toHaveURL(/\/clients$/);

    await page.getByRole("button", { name: "設定メニューを開く" }).click();

    // 設定ドロップダウン内のリンク
    await page.getByRole("link", { name: "Notion 連携" }).click();

    await expect(page).toHaveURL(/\/settings\/notion$/);
    await expect(page.getByTestId("notion-integration-page")).toBeVisible();
    await expect(
      page.getByRole("heading", { name: "Notion 連携", level: 1 }),
    ).toBeVisible();
  });
});
