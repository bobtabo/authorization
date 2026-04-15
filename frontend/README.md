<p align="center">
<a href="https://react.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original-wordmark.svg" height="72" alt="React"></a>
&nbsp;&nbsp;
<a href="https://nextjs.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/nextjs/nextjs-original-wordmark.svg" height="72" alt="Next.js"></a>
&nbsp;&nbsp;
<a href="https://www.typescriptlang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" height="72" alt="TypeScript"></a>
&nbsp;&nbsp;
<a href="https://tailwindcss.com/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tailwindcss/tailwindcss-original.svg" height="72" alt="Tailwind CSS"></a>
&nbsp;&nbsp;
<a href="https://vite.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/vitejs/vitejs-original.svg" height="72" alt="Vite"></a>
</p>

<p align="center">
<a href="https://react.dev/"><img src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white" alt="React 19"></a>
<a href="https://nextjs.org/"><img src="https://img.shields.io/badge/Next.js-15-000000?logo=nextdotjs&logoColor=white" alt="Next.js 15"></a>
<a href="https://www.typescriptlang.org/"><img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white" alt="TypeScript 5.9"></a>
<a href="https://tailwindcss.com/"><img src="https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?logo=tailwindcss&logoColor=white" alt="Tailwind CSS 4"></a>
<a href="https://vite.dev/"><img src="https://img.shields.io/badge/Vite-7-646CFF?logo=vite&logoColor=white" alt="Vite 7"></a>
</p>

---

## :book: 概要

認可サーバーの **フロントエンド** 実装です。

スタッフ向けの管理コンソール（クライアント管理・スタッフ管理・通知）を提供します。  
バックエンドとの通信は Lambda ローカルサーバー経由で行います。  
API 仕様は [`docs/api-spec/openapi.yml`](../docs/api-spec/openapi.yml) を参照してください。

---

## :building_construction: アーキテクチャ

```
ブラウザ
    │
    ▼
React Router（app/）
    │  ページルーティング
    ▼
Page / Component（app/ / components/）
    │  UI レンダリング
    ▼
API クライアント（src/api/）
    │  axios による HTTP リクエスト
    ▼
Vite Dev Proxy（/function/*）
    │  Lambda ローカルサーバー（Port:9000）へ転送
    ▼
バックエンド API
```

---

## :file_folder: ディレクトリ構成

```
frontend/
├── app/                    # ページコンポーネント（ルーティング）
│   ├── clients/            # クライアント管理
│   ├── staffs/             # スタッフ管理
│   ├── invitation/         # 招待
│   ├── login/              # ログイン
│   └── layout.tsx          # 共通レイアウト
├── components/             # 共通 UI コンポーネント
├── hooks/                  # カスタムフック
├── lib/                    # ユーティリティ
├── src/
│   ├── api/                # axios API クライアント
│   └── main.tsx            # エントリーポイント
├── e2e/                    # Playwright E2E テスト
├── vite.config.ts          # Vite 設定（Lambda プロキシ）
└── tailwind.config.ts      # Tailwind CSS 設定
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `react` | UI フレームワーク |
| `react-router-dom` | クライアントサイドルーティング |
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
cp .env.example .env
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

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-frontend.sh up    # 起動
bin/docker-frontend.sh down  # 停止
bin/docker-frontend.sh exec  # コンテナに入る
```
