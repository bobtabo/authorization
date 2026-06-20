import { test as base, expect, type Page } from "@playwright/test";

export { expect };
export type { Page };

/** real-* プロジェクト時に true になるフィクスチャ付きの test。 */
export const test = base.extend<{ isReal: boolean }>({
  isReal: async ({}, use, testInfo) => {
    await use(testInfo.project.name.startsWith("real-"));
  },
});

/**
 * auth/me・通知など、全ページ共通のモックを設定します。real モードでも常にモック。
 * isReal=true のとき staff_id=1 のクッキーを付与して実バックエンドへの書き込み操作が動くようにします。
 */
export async function mockCommon(page: Page, apiPrefix: string, isReal = false): Promise<void> {
  if (isReal) {
    await page.context().addCookies([
      { name: "staff_id", value: "1", domain: "127.0.0.1", path: "/" },
    ]);
  }
  await page.route(`${apiPrefix}/auth/me`, (route) =>
    route.fulfill({
      json: { staff_id: 1, name: "テストスタッフ", avatar: null, role: 1 },
    }),
  );
  await page.route(`${apiPrefix}/notifications/counts`, (route) =>
    route.fulfill({ json: { unread: 0, total: 0 } }),
  );
  await page.route(`${apiPrefix}/notifications*`, (route) =>
    route.fulfill({ json: [] }),
  );
}

/**
 * 通常モードのみ page.route() を設定するヘルパー。
 * isReal=true のとき（real-* プロジェクト）はスキップし、実バックエンドに通す。
 */
export async function stubRoute(
  page: Page,
  pattern: string,
  json: unknown,
  isReal: boolean,
): Promise<void> {
  if (!isReal) {
    await page.route(pattern, (route) => route.fulfill({ json }));
  }
}

const mockPager = {
  count: 2, limit: 10, next: false, previous: false,
  page: 1, nextPage: 1, previousPage: 1, pageCount: 1,
  first: true, last: true, firstRecordCount: 1, lastRecordCount: 2,
  startPage: 1, endPage: 1,
};

const mockClientRows = [
  {
    id: 1,
    name: "株式会社アルファテック",
    status: 2,
    start_at: "2026-01-01 09:00:00",
    stop_at: null,
    created_at: "2026-01-01 09:00:00",
    updated_at: "2026-01-01 09:00:00",
  },
  {
    id: 2,
    name: "ベータソリューションズ株式会社",
    status: 1,
    start_at: "2026-01-02 09:00:00",
    stop_at: null,
    created_at: "2026-01-02 09:00:00",
    updated_at: "2026-01-02 09:00:00",
  },
];

/** クライアント一覧のモックデータ */
export const mockClients = { data: mockClientRows, pager: mockPager };

/** 論理削除済みクライアントを含むモックデータ */
export const mockClientsWithDeleted = {
  data: [
    ...mockClientRows,
    {
      id: 3,
      name: "ガンマシステム株式会社",
      status: 4,
      start_at: "2026-01-03 09:00:00",
      stop_at: "2026-12-31 18:00:00",
      created_at: "2026-01-03 09:00:00",
      updated_at: "2026-01-03 09:00:00",
      deleted_at: "2027-01-01 00:00:00",
    },
  ],
  pager: { ...mockPager, count: 3, lastRecordCount: 3 },
};

/** 論理削除済みクライアントの詳細モックデータ */
export const mockSoftDeletedClientDetail = {
  id: 3,
  name: "ガンマシステム株式会社",
  post_code: "1600001",
  pref: "東京都",
  city: "新宿区",
  address: "新宿3-3-3",
  building: "新宿タワー10F",
  tel: "0310000003",
  email: "info@gamma-system.example.com",
  status: 4,
  start_at: "2026-01-03 09:00:00",
  stop_at: "2026-12-31 18:00:00",
  created_at: "2026-01-03 09:00:00",
  updated_at: "2026-01-03 09:00:00",
  deleted_at: "2027-01-01 00:00:00",
};

/** クライアント詳細のモックデータ（edit ページの d.post_code / d.pref / d.address に合わせる） */
export const mockClientDetail = {
  id: 1,
  name: "株式会社アルファテック",
  post_code: "1000001",
  pref: "東京都",
  city: "千代田区",
  address: "丸の内1-1-1",
  building: "丸の内ビル3F",
  tel: "0310000001",
  email: "info@alpha-tech.example.com",
  status: 2,
  start_at: "2026-01-01 09:00:00",
  stop_at: null,
  created_at: "2026-01-01 09:00:00",
  updated_at: "2026-01-01 09:00:00",
};

/** ログアウトのモックを設定します。 */
export async function mockLogout(page: Page, apiPrefix: string): Promise<void> {
  await page.route(`${apiPrefix}/auth/logout`, (route) =>
    route.fulfill({ status: 200, json: {} }),
  );
}

/** バックエンドランタイム定義。 */
export const BACKENDS = [
  { value: "php",       label: "PHP",           apiPrefix: "**/function/php/api" },
  { value: "go-gin",    label: "Go (Gin)",      apiPrefix: "**/function/go-gin/api" },
  { value: "go-beego",  label: "Go (Beego)",    apiPrefix: "**/function/go-beego/api" },
  { value: "go-echo",   label: "Go (Echo)",     apiPrefix: "**/function/go-echo/api" },
  { value: "kotlin",    label: "Kotlin",        apiPrefix: "**/function/kotlin/api" },
  { value: "python",    label: "Python",        apiPrefix: "**/function/python/api" },
  { value: "rb-hanami", label: "Ruby (Hanami)", apiPrefix: "**/function/rb-hanami/api" },
  { value: "rb-rails",  label: "Ruby (Rails)",  apiPrefix: "**/function/rb-rails/api" },
  { value: "rust",      label: "Rust",          apiPrefix: "**/function/rust/api" },
  { value: "ts",        label: "TypeScript",    apiPrefix: "**/function/ts/api" },
] as const;

/** スタッフ一覧のモックデータ */
export const mockStaffs = {
  data: [
    {
      id: 1,
      name: "田中 太郎",
      email: "tanaka.taro@example.com",
      role: 1,
      status: 1,
      created_at: "2026-01-01 09:00:00",
      updated_at: "2026-01-01 09:00:00",
    },
    {
      id: 2,
      name: "佐藤 花子",
      email: "sato.hanako@example.com",
      role: 2,
      status: 1,
      created_at: "2026-01-02 09:00:00",
      updated_at: "2026-01-02 09:00:00",
    },
  ],
  pager: mockPager,
};
