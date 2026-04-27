import { test, expect, stubRoute, mockCommon, mockStaffs, BACKENDS } from "./helpers";

for (const backend of BACKENDS) {
  const API = backend.apiPrefix;

  test.describe(`スタッフ [${backend.label}]`, () => {
    test.beforeEach(async ({ page, isReal }) => {
      await mockCommon(page, API);
      await stubRoute(page, `${API}/staffs*`, mockStaffs, isReal);
      await page.addInitScript((runtime) => {
        localStorage.setItem("backend-runtime", runtime);
      }, backend.value);
      await page.goto("/staffs");
    });

    test("スタッフ一覧が表示される", async ({ page }) => {
      await expect(page.getByText("スタッフ一覧")).toBeVisible();
      await expect(page.getByRole("table").getByText("テストスタッフ")).toBeVisible();
      await expect(page.getByRole("table").getByText("メンバースタッフ")).toBeVisible();
    });

    test("ロールバッジが表示される", async ({ page }) => {
      await expect(
        page.getByLabel("テストスタッフの権限").getByRole("button", { name: "管理者" }),
      ).toBeVisible();
      await expect(
        page.getByLabel("メンバースタッフの権限").getByRole("button", { name: "メンバー" }),
      ).toBeVisible();
    });

    test("名前で検索できる", async ({ page }) => {
      await page.getByPlaceholder("名前・メールで検索").fill("テストスタッフ");

      await expect(page.getByRole("table").getByText("テストスタッフ")).toBeVisible();
      await expect(page.getByRole("table").getByText("メンバースタッフ")).not.toBeVisible();
    });

    test("条件クリアで検索が解除される", async ({ page }) => {
      await page.getByPlaceholder("名前・メールで検索").fill("テストスタッフ");
      await page.getByText("条件クリア").click();

      await expect(page.getByRole("table").getByText("テストスタッフ")).toBeVisible();
      await expect(page.getByRole("table").getByText("メンバースタッフ")).toBeVisible();
    });

    test("ロール変更が実行される", async ({ page, isReal }) => {
      await stubRoute(
        page,
        `${API}/staffs/2/updateRole`,
        { ...mockStaffs.items[1], role: 1 },
        isReal,
      );

      await page.getByLabel("メンバースタッフの権限").getByRole("button", { name: "管理者" }).click();
    });

    test("スタッフを無効化できる", async ({ page, isReal }) => {
      await stubRoute(page, `${API}/staffs/2/delete`, {}, isReal);

      await page.getByLabel("メンバースタッフの状態").getByRole("button", { name: "無効" }).click();
    });
  });
}
