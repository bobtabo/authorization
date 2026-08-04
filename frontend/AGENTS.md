<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

# フロントエンドの構成ルール

このディレクトリは **Feature-Based Architecture ＋ Custom Hooks** 構成を採用している。
新機能の追加・既存機能の修正では、以下のルールを崩さないこと。

## :file_folder: ディレクトリ構成

```
frontend/
├── app/                        # App Router。薄いラッパーのみ
│   └── staffs/page.tsx         # features/ のコンポーネントを import して返すだけ
├── features/                   # 機能単位のロジックとUI
│   └── <name>/
│       ├── api.ts              # この機能専用の API 呼び出し
│       ├── types.ts            # この機能専用の型・定数
│       ├── hooks/useXxx.ts     # 状態管理・副作用・イベントハンドラ
│       └── components/Xxx.tsx  # JSX（hooks を呼ぶだけ）
└── shared/                     # 機能横断の部品
    ├── components/             # 複数 feature ／app shell で使う UI
    ├── hooks/                  # 汎用フック
    ├── lib/                    # ユーティリティ・Context
    └── api/                    # HTTP クライアント基盤と共通 API
```

- `app/` にはロジックを書かない。`page.tsx` / `layout.tsx` は
  `features/` のコンポーネントを import して返すだけの薄いラッパーに留める
  （`app/api/` の Route Handler と `app/*/callback/route.ts` はサーバー側処理なので例外）
- ロジック（状態・副作用・イベントハンドラ）は `features/<name>/hooks/` に書く
- UI（JSX）は `features/<name>/components/` に書く。データ取得や更新は hooks の戻り値から受け取る
- 2 つ以上の feature から使うもの、または app shell（ヘッダー・フッター・レイアウト等）から
  使うものは `shared/` に置く
- import は `@/features/...` / `@/shared/...` のパスエイリアスを使う。
  同一 feature 内は相対パス（`../api`、`../types`）でよい

## :sparkles: 新規ページを追加する手順

例として `features/notifications` に一覧ページを追加する場合:

1. `features/notifications/api.ts` に API 関数を書く
   （`shared/api/http.ts` の `apiGet` / `apiPost` などを使う。
   共通 API で足りる場合は `api.ts` を作らなくてよい）
2. `features/notifications/types.ts` に型・定数を書く
3. `features/notifications/hooks/useNotificationList.ts` にロジックを書く
4. `features/notifications/components/NotificationListPage.tsx` に JSX を書く
5. `app/notifications/page.tsx` は 4 を import して返すだけにする

```tsx
// app/notifications/page.tsx
import React from "react";

import { NotificationListPage } from "@/features/notifications/components/NotificationListPage";

export default function Page(): React.JSX.Element {
  return <NotificationListPage />;
}
```

## :no_entry_sign: features/ 間の直接 import 禁止

- `features/a` から `features/b` を import してはいけない
- 型や API を共有したい場合は `shared/`（`shared/api/types.ts` など）に上げてから両方から参照する
- UI を共有したい場合も `shared/components/` に移動する
