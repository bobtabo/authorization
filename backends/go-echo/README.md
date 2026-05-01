<p align="center">
<a href="https://go.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/go/go-original-wordmark.svg" height="72" alt="Go"></a>
&nbsp;&nbsp;
<a href="https://echo.labstack.com/" target="_blank"><img src="https://avatars.githubusercontent.com/u/2624634" height="72" alt="Echo"></a>
&nbsp;&nbsp;
<a href="https://entgo.io/" target="_blank"><img src="https://entgo.io/img/logo.png" height="30" alt="ent" style="vertical-align: top; margin-top: 18px;"></a>
</p>

<p align="center">
<a href="https://go.dev/dl/"><img src="https://img.shields.io/badge/Go-1.24.0-00ADD8?logo=go&logoColor=white" alt="Go 1.24.0"></a>
<a href="https://github.com/labstack/echo"><img src="https://img.shields.io/badge/Echo-v4.13.3-4AE1FF?logo=echo&logoColor=white" alt="Echo v4.13.3"></a>
<a href="https://entgo.io/"><img src="https://img.shields.io/badge/ent-v0.14.6-8DBED9?logo=ent&logoColor=white" alt="ent v0.14.6"></a>
</p>

---

## :book: 概要

認可サーバー API の **Go / Echo** バックエンド実装です。

Google OAuth 2.0 によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。  
PHP / Laravel 実装と同一 MySQL スキーマを共有し、完全な機能互換を持ちます。  
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

---

## :building_construction: アーキテクチャ

DDD + クリーンアーキテクチャを採用しています。

```
HTTP Request
    │
    ▼
Handler (internal/handler/)
    │  リクエスト解析・レスポンス整形（echo.Context）
    ▼
UseCase / Interactor (internal/usecase/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    │  Domain Repository インターフェースに依存（依存性逆転）
    ▼
Domain (internal/domain/)
    │  エンティティ・リポジトリインターフェース・値オブジェクト
    ▼
Infrastructure (internal/infrastructure/)
    │  ent 実装リポジトリ・Redis キャッシュ
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
backends/go-echo/
├── ent/                             # ent 生成コード
│   ├── schema/                      # エンティティスキーマ定義
│   └── ...                          # ent 生成ファイル（コミット対象）
├── internal/
│   ├── config/                  # 環境変数読み込み（godotenv）
│   ├── domain/                  # ドメイン層
│   │   ├── client/              # エンティティ・リポジトリ IF・値オブジェクト・条件・列挙
│   │   ├── staff/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/                # 値オブジェクト・キャッシュリポジトリ IF
│   ├── usecase/                 # ユースケース層
│   │   ├── client/              # DTO・インタラクター
│   │   ├── staff/
│   │   ├── auth/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   ├── infrastructure/          # インフラ層
│   │   ├── persistence/         # ent リポジトリ実装
│   │   ├── cache/               # Redis キャッシュリポジトリ実装
│   │   └── db/                  # ent 接続
│   ├── handler/                 # Echo ハンドラー層（echo.Context）
│   └── middleware/              # 認証・エラーハンドリングミドルウェア
├── pkg/
│   └── apperror/                # アプリケーションエラー定義
├── tests/                       # 結合テスト
├── main.go                      # エントリーポイント・DI 組み立て・ルーティング
├── go.mod
└── go.sum
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `labstack/echo/v4` | HTTP フレームワーク |
| `entgo.io/ent` | ORM（コード生成型） |
| `go-sql-driver/mysql` | MySQL ドライバー |
| `golang-jwt/jwt/v5` | JWT 生成・検証（RS256） |
| `redis/go-redis/v9` | Redis クライアント |
| `golang.org/x/oauth2` | Google OAuth 2.0 |
| `joho/godotenv` | `.env` 読み込み |

---

## :rocket: セットアップ

### 1. 依存パッケージの取得

```bash
go mod tidy
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
go run main.go
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-go-echo.sh up    # 起動
bin/docker-go-echo.sh down  # 停止
bin/docker-go-echo.sh exec  # コンテナに入る
```

---

## :test_tube: テスト

```bash
go test ./tests/... -v
```

> テスト実行には MySQL・Redis への接続が必要です。  
> `tests/setup_test.go` で接続先を確認してください。
