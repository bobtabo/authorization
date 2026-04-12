import { test, expect } from "@playwright/test";
import { mockCommon, mockClients, BACKENDS } from "./helpers";

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
});

for (const backend of BACKENDS) {
  test.describe(`認証 [${backend.label}]`, () => {
    test("E2E モードでログインするとクライアント一覧へ遷移する", async ({ page }) => {
      await mockCommon(page, backend.apiPrefix);
      await page.route(`${backend.apiPrefix}/clients*`, (route) =>
        route.fulfill({ json: mockClients }),
      );
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
