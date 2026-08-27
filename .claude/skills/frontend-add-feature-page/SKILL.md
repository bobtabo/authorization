---
name: frontend-add-feature-page
description: >-
  frontend/ に新しいページや機能を追加・修正する際に使う。Feature-Based Architecture
  ＋ Custom Hooks 構成に沿って api.ts → types.ts → hooks/ → components/ →
  app/<route>/page.tsx の順に実装し、lint / build / E2E で確認する手順を定める。
allowed-tools: Bash(npm:*), Bash(npx:*), Bash(env:*), Bash(git:*), Bash(rg:*), Bash(awk:*)
---

# frontend-add-feature-page

`frontend/AGENTS.md` の構成ルールを実作業の手順に落としたもの。ルールの正本は
`frontend/AGENTS.md`（および `frontend/CLAUDE.md`）。矛盾したらそちらに従う。

**重要**: このリポジトリの Next.js は学習データと異なる破壊的変更を含むバージョン。
App Router 周り（`page.tsx` / `layout.tsx` / Route Handler / `params` の扱い等）を書く前に
`frontend/node_modules/next/dist/docs/` の該当ガイドを読む。既存の `app/**/page.tsx` を
そのまま踏襲するのが最も安全。

## 1. 構成と責務

```
frontend/
├── app/<route>/page.tsx        # 薄いラッパー。features/ のコンポーネントを返すだけ
├── features/<name>/
│   ├── api.ts                  # この機能専用のAPI呼び出し
│   ├── types.ts                # この機能専用の型・定数
│   ├── hooks/useXxx.ts         # 状態・副作用・イベントハンドラ
│   └── components/Xxx.tsx      # JSX（hooksを呼ぶだけ）
└── shared/{api,components,hooks,lib}/   # 機能横断の部品
```

- `app/` にロジックを書かない（例外: `app/api/` の Route Handler と
  `app/*/callback/route.ts` はサーバー側処理）。
- ロジックは `features/<name>/hooks/`、JSX は `features/<name>/components/`。
  コンポーネント内で `useState` / `useEffect` / fetch を直接書かない。
- **`features/a` から `features/b` を import しない**。共有したい型・API・UIは
  `shared/` に上げてから両方から参照する。
- import は `@/features/...` / `@/shared/...` のエイリアス。同一feature内は相対パス（`../api`）。
- 命名は既存に合わせる。`features/<name>/components/` はPascalCase（`StaffListPage.tsx`）、
  `shared/components/` はkebab-case（`confirm-dialog.tsx`）で、実際にこの2系統が混在している。
  **新規ファイルは配置先の既存ファイルに合わせる**。

現状の feature は `auth` / `clients` / `staffs` / `errors`。参考実装として
`features/staffs`（`api.ts` + `types.ts` + `hooks/useStaffList.ts` +
`components/StaffListPage.tsx` + `app/staffs/page.tsx`）が最小構成で読みやすい。

## 2. 追加手順

例: 通知一覧ページ `features/notifications` を追加する場合。

1. **api.ts**: `shared/api/http.ts` の `apiGet` / `apiPost` 等を使ってAPI関数を書く。
   `shared/api/` の共通APIで足りるなら `api.ts` は作らない。
   エンドポイント・型は `docs/api-spec/openapi.yml` に合わせる（勝手に決めない）。
2. **types.ts**: この機能専用の型・定数。複数featureで使うなら `shared/api/types.ts` へ。
3. **hooks/useNotificationList.ts**: 状態管理・データ取得・イベントハンドラ。
   エラーは `shared/lib/api-error.ts` の既存ハンドリングに合わせる。
4. **components/NotificationListPage.tsx**: JSXのみ。hooks の戻り値を描画する。
   ページャは `shared/components/pager.tsx`、確認ダイアログは `confirm-dialog.tsx` 等、
   既存の共通部品を再利用する（同等のものを作らない）。
5. **app/notifications/page.tsx**: import して返すだけ。

```tsx
// app/notifications/page.tsx
import React from "react";

import { NotificationListPage } from "@/features/notifications/components/NotificationListPage";

export default function Page(): React.JSX.Element {
  return <NotificationListPage />;
}
```

6. ナビゲーションから遷移させる場合は `shared/components/console-header.tsx` 等の
   app shell 側にリンクを追加する。
7. 画面遷移が増える場合は `docs/ui-flow/` と `docs/architecture/frontend.puml` の
   整合を確認し、必要なら更新する。

## 3. 確認（コード変更時は必須）

lint / build はホスト側で実行する（フロントエンドはコンテナ不要）。
`cd` や環境変数の前置きで始まる形にしない（`allowed-tools` の先頭一致に外れて
毎回手動承認になるため）。`npm --prefix frontend` と `env` を使ったワンライナーで実行する:

```bash
npm --prefix frontend run lint
env NEXT_PUBLIC_API_URL=/function/php/api npm --prefix frontend run build
```

E2E（モック版、CIと同じ範囲）:

```bash
env CI=true NEXT_PUBLIC_API_URL=/function/php/api npm --prefix frontend run test:e2e
```

- E2Eは専用ポート `127.0.0.1:3001` で起動するので `npm run dev`（3000）と併走できる。
- `NEXT_PUBLIC_E2E=1` でログイン画面の「Googleで続行」がモック遷移になる。
- 郵便番号検索のテストは `NEXT_PUBLIC_POSTCODE_API_KEY` が無いと失敗する（既知の制約）。
  この失敗のみであれば新規変更の問題ではないが、報告時に明示する。
- ブラウザが見つからない場合は
  `env -u PLAYWRIGHT_BROWSERS_PATH npx playwright install chromium` の後に再実行。
- 実バックエンドE2E（`--project=real-go-gin` など）は LocalStack + Lambda 起動と
  `e2e/seed.sql` 適用が前提。CIでは実行されない。起動手順は docker-ops Skill
  （Issue #168 / PR #176）。**同時期に追加されるSkillなので、それが `develop` に
  マージされる前は参照先が存在しない場合がある**。その場合の暂定手順:

  ```bash
  docker/bin/docker-common.sh up      # 共通インフラ（MySQL / Redis / LocalStack）
  docker/bin/docker-go-gin.sh up      # 対象バックエンド
  ```

  （初回は `docker/bin/docker-common.sh env` で証明書と `.env` を配置する。
  停止・破棄は必ずラッパーの `down` を使い、実行前にユーザーに一声かける）
- `frontend/e2e/demo/` はデモGIF録画専用。CIでは実行しないし、通常の確認でも実行しない。
- 新規ページを追加したら、E2Eテスト（`frontend/e2e/<name>.spec.ts`）も追加する。
  既存specの `helpers.ts` のログインヘルパを流用する。

## 4. 落とし穴

- **`app/` にロジックが漏れる**: `page.tsx` に `useState` や fetch を書いてしまうのが最多の逸脱。
- **feature間import**: 既存featureの型を使いたくなったら `shared/` に上げる。
  この違反は `git diff` で `@/features/` の import を検索すると見つかる:

  ```bash
  # 「自分以外のfeatureをimportしている行」だけを出す（rgは後方参照非対応なのでawkで判定）
  rg -n --no-heading 'from "@/features/' frontend/features \
    | awk -F: '{ split($1, p, "/"); match($0, /@\/features\/[^\/"]+/);
                 t = substr($0, RSTART + 11, RLENGTH - 11);
                 if (t != p[3]) print }' \
    || true
  ```

  この検出は現時点で lint ルールになっていない（`frontend/eslint.config.mjs` に
  `no-restricted-imports` 等の強制がない）ため、**`npm run lint` / CI では止まらない**。
  featureを追加・変更したときは上のコマンドを自分で実行する（lintルール化は別Issue）。

- **未使用import・未使用変数**: `eslint-plugin-unused-imports` で lint エラーになる。
- **`NEXT_PUBLIC_API_URL`**: `docker-localstack-init.sh` が `frontend/.env.local` を生成する。
  手で書き換えず、バックエンドを切り替えたい場合は生成側の仕組みを使う。
- 日時表示は `shared/lib/format-datetime.ts` を使う（独自フォーマットを書かない）。
