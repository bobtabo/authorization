<p align="center">
<a href="https://www.typescriptlang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" height="64" alt="TypeScript"></a>
&nbsp;&nbsp;
<a href="https://hono.dev/" target="_blank"><img src="https://hono.dev/images/logo.png" height="64" alt="Hono"></a>
</p>

<p align="center">
<a href="https://www.typescriptlang.org/"><img src="https://img.shields.io/badge/TypeScript-5.8.3-3178C6?logo=typescript&logoColor=white" alt="TypeScript 5.8.3"></a>
<a href="https://hono.dev/"><img src="https://img.shields.io/badge/Hono-4.7.11-FF5B02?logo=hono&logoColor=white" alt="Hono 4.7.11"></a>
<a href="https://orm.drizzle.team/"><img src="https://img.shields.io/badge/Drizzle_ORM-0.43.1-C5F74F?logo=drizzle&logoColor=black" alt="Drizzle ORM 0.43.1"></a>
</p>

---

## :book: 概要

認可サーバー API の **TypeScript / Hono** バックエンド実装です。

Google OAuth 2.0 によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。  
PHP / Laravel 実装と同一 MySQL スキーマを共有し、完全な機能互換を持ちます。  
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

---

## :building_construction: アーキテクチャ

レイヤードアーキテクチャを採用しています。

```
HTTP Request
    │
    ▼
Route (src/routes/)
    │  リクエスト解析・レスポンス整形
    ▼
Service (src/services/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    ▼
Repository (src/repositories/)
    │  データアクセス抽象化
    ▼
Drizzle ORM
    │  MySQL2 ドライバー
    ▼
MySQL / Redis
```

### ドメイン一覧

| ドメイン | 責務 |
|---|---|
| **Auth** | Google OAuth 2.0 認証・Cookie セッション管理・招待トークン検証 |
| **Client** | クライアントの CRUD・RSA 4096bit 鍵ペア生成・SSH fingerprint |
| **Gate** | JWT 発行 (`/gate/issue`) と検証 (`/gate/verify`) |
| **Invitation** | スタッフ招待 URL の発行・管理 |
| **Notification** | スタッフへの通知配信・カーソルページネーション・既読管理 |
| **Staff** | スタッフアカウントの管理・ロール変更・論理削除 |

---

## :file_folder: ディレクトリ構成

```
backends/ts-hono/
├── src/
│   ├── db/
│   │   ├── client.ts       # Drizzle + mysql2 接続
│   │   └── schema.ts       # テーブルスキーマ定義
│   ├── lib/
│   │   ├── cookie.ts       # Cookie ヘルパー・時刻フォーマット
│   │   ├── errors.ts       # AppError 定義
│   │   └── redis.ts        # ioredis クライアント
│   ├── repositories/       # データアクセス実装
│   ├── routes/             # Hono ルーター
│   ├── services/           # ビジネスロジック実装
│   ├── config.ts           # 環境変数管理
│   └── index.ts            # エントリーポイント・サーバー起動
├── package.json
└── tsconfig.json
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `hono` | HTTP フレームワーク |
| `@hono/node-server` | Node.js アダプター |
| `drizzle-orm` | ORM |
| `mysql2` | MySQL ドライバー |
| `jose` | JWT 生成・検証（RS256） |
| `ioredis` | Redis クライアント |
| `tsx` | TypeScript 実行・ホットリロード |

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

以下を設定してください。

```dotenv
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### 3. 起動

```bash
npm run dev
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-ts.sh up    # 起動
bin/docker-ts.sh down  # 停止
bin/docker-ts.sh exec  # コンテナに入る
```
