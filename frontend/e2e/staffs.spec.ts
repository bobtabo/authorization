import { test, expect, stubRoute, mockCommon, mockStaffs, BACKENDS } from "./helpers";

for (const backend of BACKENDS) {
  const API = backend.apiPrefix;

  test.describe(`スタッフ [${backend.label}]`, () => {
    test.beforeEach(async ({ page, isReal }) => {
      await mockCommon(page, API, isReal);
      await stubRoute(page, `${API}/staffs*`, mockStaffs, false);
      await page.addInitScript((runtime) => {
        localStorage.setItem("backend-runtime", runtime);
      }, backend.value);
      await page.goto("/staffs");
    });

    test("スタッフ一覧が表示される", async ({ page }) => {
      await expect(page.getByText("スタッフ一覧")).toBeVisible();
      await expect(page.getByRole("table").getByText("田中 太郎")).toBeVisible();
      await expect(page.getByRole("table").getByText("佐藤 花子")).toBeVisible();
    });

    test("ロールバッジが表示される", async ({ page }) => {
      await expect(
        page.getByLabel("田中 太郎の権限").getByRole("button", { name: "管理者" }),
      ).toBeVisible();
      await expect(
        page.getByLabel("佐藤 花子の権限").getByRole("button", { name: "メンバー" }),
      ).toBeVisible();
    });

    test("名前で検索できる", async ({ page }) => {
      await page.getByPlaceholder("名前・メールで検索").fill("田中");

      await expect(page.getByRole("table").getByText("田中 太郎")).toBeVisible();
      await expect(page.getByRole("table").getByText("佐藤 花子")).not.toBeVisible();
    });

    test("条件クリアで検索が解除される", async ({ page }) => {
      await page.getByPlaceholder("名前・メールで検索").fill("田中");
      await page.getByText("条件クリア").click();

      await expect(page.getByRole("table").getByText("田中 太郎")).toBeVisible();
      await expect(page.getByRole("table").getByText("佐藤 花子")).toBeVisible();
    });

    test("ロール変更が実行される", async ({ page, isReal }) => {
      await stubRoute(
        page,
        `${API}/staffs/2/updateRole`,
        { ...mockStaffs.items[1], role: 1 },
        isReal,
      );

      await page.getByLabel("佐藤 花子の権限").getByRole("button", { name: "管理者" }).click();
    });

    test("スタッフを無効化できる", async ({ page, isReal }) => {
      await stubRoute(page, `${API}/staffs/2/delete`, {}, isReal);

      await page.getByLabel("佐藤 花子の状態").getByRole("button", { name: "無効" }).click();
    });
  });
}
