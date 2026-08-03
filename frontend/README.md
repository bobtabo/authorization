<p align="center">
<a href="https://react.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original-wordmark.svg" height="72" alt="React"></a>
&nbsp;&nbsp;
<a href="https://nextjs.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/nextjs/nextjs-original-wordmark.svg" height="72" alt="Next.js"></a>
&nbsp;&nbsp;
<a href="https://www.typescriptlang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" height="72" alt="TypeScript"></a>
&nbsp;&nbsp;
<a href="https://tailwindcss.com/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tailwindcss/tailwindcss-original.svg" height="72" alt="Tailwind CSS"></a>
</p>

<p align="center">
<a href="https://react.dev/"><img src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white" alt="React 19"></a>
<a href="https://nextjs.org/"><img src="https://img.shields.io/badge/Next.js-16-000000?logo=nextdotjs&logoColor=white" alt="Next.js 16"></a>
<a href="https://www.typescriptlang.org/"><img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white" alt="TypeScript 5.9"></a>
<a href="https://tailwindcss.com/"><img src="https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?logo=tailwindcss&logoColor=white" alt="Tailwind CSS 4"></a>
</p>

---

## :book: 概要

認可サーバーの **フロントエンド** 実装です。

スタッフ向けの管理コンソール（クライアント管理・スタッフ管理・通知）を提供します。  
バックエンドとの通信は LocalStack API Gateway 経由で行います。  
API 仕様は [`docs/api-spec/openapi.yml`](../docs/api-spec/openapi.yml) を参照してください。

---

## :building_construction: アーキテクチャ

```
ブラウザ
    │
    ▼
Next.js App Router（app/）
    │  ページルーティング（薄いラッパーのみ）
    ▼
Component（features/<name>/components/ / shared/components/）
    │  UI レンダリング
    ▼
Custom Hook（features/<name>/hooks/）
    │  状態管理・副作用
    ▼
API クライアント（features/<name>/api.ts / shared/api/）
    │  axios による HTTP リクエスト
    ▼
Next.js Rewrites（/function/*）
    │  LocalStack API Gateway（Port:4566）へ転送
    ▼
バックエンド API
```

---

## :file_folder: ディレクトリ構成

**Feature-Based Architecture ＋ Custom Hooks** 構成です。
`app/` は薄いルーティングのみで、ロジックと UI は `features/<name>/` に集約しています。

```
frontend/
├── app/                    # ルーティング（App Router）。features/ を import して返すだけ
│   ├── clients/            # クライアント管理
│   ├── staffs/             # スタッフ管理
│   ├── invitation/         # 招待受諾
│   ├── login/              # ログイン
│   ├── register/           # 新規登録（招待制の案内）
│   ├── api/                # Route Handler（サーバー側処理）
│   └── layout.tsx          # 共通レイアウト
├── features/               # 機能単位のロジックとUI（auth / clients / staffs）
│   └── <name>/             # auth・clients・staffs はいずれも同じ内部構成
│       ├── api.ts          # この機能専用の API 呼び出し（auth は shared/api のみ使用のため無し）
│       ├── types.ts        # この機能専用の型・定数
│       ├── hooks/          # カスタムフック（状態管理・副作用）
│       └── components/     # UI コンポーネント（JSX）
├── shared/                 # 機能横断の部品
│   ├── components/         # 共通 UI コンポーネント
│   ├── hooks/              # 汎用フック
│   ├── lib/                # ユーティリティ・Context
│   └── api/                # axios API クライアント
├── e2e/                    # Playwright E2E テスト
│   └── demo/               # デモ録画用シナリオ
├── scripts/                # ユーティリティスクリプト
├── next.config.ts          # Next.js 設定（API Gateway プロキシ）
└── tailwind.config.ts      # Tailwind CSS 設定
```

構成ルール（`features/` 間の直接 import 禁止、新規ページ追加手順など）は
[`AGENTS.md`](./AGENTS.md) を参照してください。

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `next` | フレームワーク（App Router・SSR） |
| `react` | UI ライブラリ |
| `axios` | HTTP クライアント |
| `tailwindcss` | ユーティリティファースト CSS |
| `framer-motion` | アニメーション |
| `lucide-react` | アイコン |
| `@playwright/test` | E2E テスト |

---

## :rocket: セットアップ

### 1. 依存パッケージのインストール

```bash
npm install
```

### 2. 環境変数の設定

```bash
# LocalStack モード（推奨）: make apply 時に .env が自動生成される
# 手動で作成する場合:
cp .env.localstack .env
# .env 内の {api-id} を実際の API Gateway ID に置換する

# emulator モード（非推奨）:
# cp .env.emulator .env
```

### 3. 起動

```bash
npm run dev
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :test_tube: テスト

```bash
# E2E テスト（Playwright）
npm run test:e2e

# UI モードで実行
npm run test:e2e:ui
```

---

## :movie_camera: デモ動画（GIF）の生成

Playwright でデモ用シナリオを録画し、GIF に変換できます。

### 前提

- バックエンドコンテナ（PHP + MailPit）が起動済みであること
- seed データが投入済みであること（`php artisan migrate --seed`）
- ffmpeg がインストール済みであること（GIF 変換に使用）

```bash
# macOS（Homebrew）
brew install ffmpeg
```

```bash
# 1. コンテナ起動
bin/docker-common.sh up
bin/docker-php-laravel.sh up

# 2. フロントエンド dev サーバー起動（E2E 用ポート 3001）
cd frontend
NEXT_PUBLIC_E2E=1 npm run dev -- --port 3001
```

### 1. デモ動画の録画

```bash
cd frontend
npx playwright test --project=demo
```

録画ファイルは `test-results/` 配下に `.webm` 形式で保存されます。
フォルダ名はテストファイル名・テスト名・プロジェクト名から自動生成されます
（例: `test-results/authorization-flow-認可フロー全体のデモ録画-demo/video.webm`）。

### 2. GIF 変換

```bash
# ffmpeg が必要です
bash scripts/video-to-gif.sh test-results/authorization-flow-認可フロー全体のデモ録画-demo/video.webm demo.gif
```

`demo.gif` はリポジトリルートの README から `./frontend/demo.gif` として参照されています。

> [!NOTE]
>
> デモシナリオは CI では実行されません。ローカル環境でのみ実行してください。
> GIF は README または Notion に埋め込み可能なサイズ（5MB 以下）に収めてください。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-frontend.sh up    # 起動
bin/docker-frontend.sh down  # 停止
bin/docker-frontend.sh exec  # コンテナに入る
```
