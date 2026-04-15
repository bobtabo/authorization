<p align="center">
<a href="https://go.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/go/go-original-wordmark.svg" height="72" alt="Go"></a>
&nbsp;&nbsp;
<a href="https://gin-gonic.com/" target="_blank"><img src="https://raw.githubusercontent.com/gin-gonic/logo/master/color.png" height="72" alt="Gin"></a>
</p>

<p align="center">
<a href="https://go.dev/dl/"><img src="https://img.shields.io/badge/Go-1.24.0-00ADD8?logo=go&logoColor=white" alt="Go 1.24.0"></a>
<a href="https://github.com/gin-gonic/gin"><img src="https://img.shields.io/badge/Gin-1.10.0-00ADD8?logo=go&logoColor=white" alt="Gin 1.10.0"></a>
<a href="https://gorm.io/"><img src="https://img.shields.io/badge/GORM-1.25.12-00ADD8?logo=go&logoColor=white" alt="GORM 1.25.12"></a>
</p>

---

## :book: 概要

認可サーバー API の **Go / Gin** バックエンド実装です。

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
Handler (internal/handler/)
    │  リクエスト解析・レスポンス整形
    ▼
Service (internal/service/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    ▼
Repository (internal/repository/)
    │  データアクセス抽象化
    ▼
GORM (gorm.io/gorm)
    │  ORM / MySQL ドライバー
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
backends/go-gin/
├── cmd/
│   └── main.go             # エントリーポイント・DI 組み立て・ルーティング
├── internal/
│   ├── config/             # 環境変数読み込み（godotenv）
│   ├── handler/            # Gin ハンドラー層
│   ├── infrastructure/
│   │   ├── cache/          # Redis クライアント
│   │   └── db/             # GORM 接続
│   ├── middleware/         # 認証・エラーハンドリングミドルウェア
│   ├── model/              # GORM モデル（DB スキーマ定義）
│   ├── repository/         # データアクセス実装
│   └── service/            # ビジネスロジック実装
├── pkg/
│   └── apperror/           # アプリケーションエラー定義
├── go.mod
└── go.sum
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `gin-gonic/gin` | HTTP フレームワーク |
| `gorm.io/gorm` | ORM |
| `gorm.io/driver/mysql` | MySQL ドライバー |
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
go run ./cmd/main.go
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-go.sh up    # 起動
bin/docker-go.sh down  # 停止
bin/docker-go.sh exec  # コンテナに入る
```
