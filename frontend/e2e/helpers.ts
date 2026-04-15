import type { Page } from "@playwright/test";

/** auth/me・通知など、全ページ共通のモックを設定します。 */
export async function mockCommon(page: Page, apiPrefix: string): Promise<void> {
  // ログイン済みユーザー（管理者）
  await page.route(`${apiPrefix}/auth/me`, (route) =>
    route.fulfill({
      json: { staff_id: 1, name: "テストスタッフ", avatar: null, role: 1 },
    }),
  );

  // 通知ヘッダー
  await page.route(`${apiPrefix}/notifications/counts`, (route) =>
    route.fulfill({ json: { unread: 0, total: 0 } }),
  );
  await page.route(`${apiPrefix}/notifications*`, (route) =>
    route.fulfill({ json: [] }),
  );
}

/** クライアント一覧のモックデータ */
export const mockClients = [
  {
    id: 1,
    name: "株式会社サンプル",
    status: 2,
    start_at: "2026-01-15 09:00",
    stop_at: null,
    created_at: "2026-01-01 00:00",
    updated_at: "2026-01-15 09:00",
  },
  {
    id: 2,
    name: "テスト商事",
    status: 1,
    start_at: null,
    stop_at: null,
    created_at: "2026-02-01 00:00",
    updated_at: "2026-02-01 00:00",
  },
];

/** 論理削除済みクライアントを含むモックデータ */
export const mockClientsWithDeleted = [
  ...mockClients,
  {
    id: 3,
    name: "アーカイブ商事",
    status: 4,
    start_at: "2025-01-01 09:00",
    stop_at: "2025-12-31 18:00",
    created_at: "2025-01-01 00:00",
    updated_at: "2026-01-01 00:00",
    deleted_at: "2026-01-01 00:00",
  },
];

/** 論理削除済みクライアントの詳細モックデータ */
export const mockSoftDeletedClientDetail = {
  id: 3,
  name: "アーカイブ商事",
  post_code: "1000001",
  pref: "東京都",
  city: "千代田区",
  address: "千代田1-1",
  building: "",
  tel: "0312345678",
  email: "archive@example.com",
  status: 4,
  start_at: "2025-01-01 09:00",
  stop_at: "2025-12-31 18:00",
  created_at: "2025-01-01 00:00",
  updated_at: "2026-01-01 00:00",
  deleted_at: "2026-01-01 00:00",
};

/** クライアント詳細のモックデータ（edit ページの d.post_code / d.pref / d.address に合わせる） */
export const mockClientDetail = {
  id: 1,
  name: "株式会社サンプル",
  post_code: "1000001",
  pref: "東京都",
  city: "千代田区",
  address: "千代田1-1",
  building: "",
  tel: "0312345678",
  email: "sample@example.com",
  status: 2,
  start_at: "2026-01-15 09:00",
  stop_at: null,
  created_at: "2026-01-01 00:00",
  updated_at: "2026-01-15 09:00",
};

/** ログアウトのモックを設定します。 */
export async function mockLogout(page: Page, apiPrefix: string): Promise<void> {
  await page.route(`${apiPrefix}/auth/logout`, (route) =>
    route.fulfill({ status: 200, json: {} }),
  );
}

/** バックエンドランタイム定義。 */
export const BACKENDS = [
  { value: "php",    label: "PHP",        apiPrefix: "**/function/php/api" },
  { value: "go",     label: "Go",         apiPrefix: "**/function/go/api" },
  { value: "python", label: "Python",     apiPrefix: "**/function/python/api" },
  { value: "ts",     label: "TypeScript", apiPrefix: "**/function/ts/api" },
] as const;

/** スタッフ一覧のモックデータ */
export const mockStaffs = {
  items: [
    {
      id: 1,
      name: "テストスタッフ",
      email: "staff@example.com",
      role: 1,
      status: 1,
      created_at: "2026-01-01 00:00",
      updated_at: "2026-01-01 00:00",
    },
    {
      id: 2,
      name: "メンバースタッフ",
      email: "member@example.com",
      role: 2,
      status: 1,
      created_at: "2026-02-01 00:00",
      updated_at: "2026-02-01 00:00",
    },
  ],
};
