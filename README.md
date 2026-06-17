# <img src="https://raw.githubusercontent.com/bobtabo/authorization/main/frontend/app/icon.svg" height="25" style="margin-top:-4px;vertical-align:middle;" alt="認可サーバー"> 認可サーバー

このリポジトリは、OAuth2/OIDC 準拠の認可サーバー構築プロジェクトです。  
各コンポーネントの詳細は、それぞれのディレクトリにあるドキュメントを参照してください。

---

## :clipboard: 目次

- [システム構造](#システム構造)
- [プロジェクト構成](#プロジェクト構成)
- [開発環境構築手順](#開発環境構築手順)
  - [前提](#前提)
  - [1. リポジトリのクローン](#1-リポジトリのクローン)
  - [2. 初回セットアップ](#2-初回セットアップ)
  - [3. 共通コンテナの起動](#3-共通コンテナの起動)
  - [4. バックエンドコンテナの起動](#4-バックエンドコンテナの起動)
  - [5. フロントエンドの起動](#5-フロントエンドの起動)
  - [6. バックエンドの初期設定](#6-バックエンドの初期設定)
  - [7. 初回ログイン](#7-初回ログイン)
- [クイックスタート](#クイックスタート)

---

## :building_construction: システム構造

```.
├── 📂 backends/           # バックエンド構成（PHP / Go / Kotlin / Python / TypeScript / Ruby / Rust）
│   ├── go-beego/
│   ├── go-echo/
│   ├── go-gin/
│   ├── kotlin-ktor/
│   ├── php-laravel/
│   ├── python-fastapi/
│   ├── ruby-hanami/
│   ├── ruby-rails/
│   ├── rust-axum/
│   └── ts-hono/
├── 📂 docker/             # コンテナ定義（BACKEND_MODE で切り替え）
├── 📂 docs/
│   ├── api-spec/          # API 仕様書（OpenAPI / Swagger UI）
│   └── ui-flow/           # 画面フロー
├── 📂 frontend/           # 認可管理画面（React / Next.js）
├── 📂 function/           # AWS Lambda 関数（Go）
├── 📂 terraform/          # Terraform IaC 定義（環境別）
│   ├── local/             # LocalStack 向け
│   ├── develop/           # 実 AWS 開発環境用（予約）
│   ├── staging/           # 実 AWS ステージング用（予約）
│   └── production/        # 実 AWS 本番用（予約）
└── 📜 README.md
```

---

## :file_folder: プロジェクト構成

| ディレクトリ              | 内容                                | ドキュメント                                 |
|:--------------------|:----------------------------------|:---------------------------------------|
| **`backends/`**     | バックエンド構成                          | [README.md](./backends/README.md)      |
| **`docker/`**       | コンテナ定義                            | [README.md](./docker/README.md)        |
| **`docs/api-spec`** | API 仕様書（OpenAPI）& Swagger UI 環境 | [README.md](./docs/api-spec/README.md) |
| **`docs/ui-flow`**  | 画面フロー                             | [README.md](./docs/ui-flow/README.md)  |
| **`frontend/`**     | 認可管理画面（React / Next.js）           | [README.md](./frontend/README.md)      |
| **`function/`**     | AWS Lambda 関数（Go）                 | [README.md](./function/README.md)      |
| **`terraform/`**    | Terraform IaC 定義（環境別）              | [README.md](./terraform/README.md)       |

---

## :hammer_and_wrench: 開発環境構築手順

### 前提

- Docker Engine / Docker Compose がインストール済みであること
- ポート `443`（プロキシ）、`3306`（MySQL）、`6379`（Redis）、`4566`（LocalStack）がローカルで空いていること
- [LocalStack CLI](https://docs.localstack.cloud/getting-started/installation/) がインストール済みであること
- [Terraform](https://developer.hashicorp.com/terraform/install) がインストール済みであること
- [tflocal](https://github.com/localstack/terraform-local)（`pip install terraform-local`）がインストール済みであること
- LocalStack の認証トークンを取得済みであること（<a href="https://app.localstack.cloud/">LocalStack Web App</a>）
- Google OAuth 2.0 のクライアント ID / シークレットを取得済みであること（<a href="https://console.cloud.google.com/">Google Cloud Console</a>）
- GitHub OAuth App のクライアント ID / シークレットを取得済みであること（<a href="https://github.com/settings/developers">GitHub Developer Settings</a>）
- [ngrok](https://ngrok.com/) がインストール済みであること（モバイル / ShowCase CI 連携時に必要）
- ngrok の固定ドメインを取得済みであること（同上、<a href="https://dashboard.ngrok.com/">ngrok ダッシュボード</a>から取得）

### 1. リポジトリのクローン

```bash
git clone git@github.com:bobtabo/authorization.git
cd authorization
```

### 2. 初回セットアップ

```bash
cd docker
find ./bin -type f -exec chmod 755 {} +
bin/docker-common.sh env
```

> 詳細（`LOCALSTACK_AUTH_TOKEN` の設定・`BACKEND_MODE` 等）は [docker/README.md](./docker/README.md) を参照。

### 3. 共通コンテナの起動

```bash
bin/docker-common.sh up
```

> モード切替・LocalStack 再デプロイ等の詳細は [docker/README.md](./docker/README.md) を参照。

### 4. バックエンドコンテナの起動

```bash
bin/docker-backends.sh up
```

#### ngrok による外部公開

```bash
ngrok http --url=your-domain.ngrok-free.dev 4566
```

### 5. フロントエンドの起動

```bash
cd frontend
npm install
npm run dev
```

> セットアップの詳細は [frontend/README.md](./frontend/README.md) を参照。

### 6. バックエンドの初期設定

使用するバックエンドのコンテナに入り、環境変数の設定とセットアップを行います。</br>
各バックエンドの詳細は `backends/*/README.md` を参照してください。

```bash
# 共通手順: コンテナに入って .env を設定
bin/docker-<backend>.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

> [!NOTE]
> 
> マイグレーションは PHP（Laravel）に一本化している。</br>
> `bin/docker-php.sh exec` → `composer install` → `php artisan migrate --seed` を最初に実行してください。</br>
> 他のバックエンドはテスト用のスキーマ定義を個別に持つ。

### 7. 初回ログイン

初回は招待リンクからアクセスする必要があります。</br>
招待リンクなしでは、ログイン画面からサインインできません。

http://localhost:3000/invitation/8f13761980983d1d9e3950d11b42016f

> [!NOTE]
> 
> このリンクはローカル環境専用です。</br>本番環境では、管理者から発行された招待リンクを使用してください。

---

## :rocket: クイックスタート

### 1. API 仕様の確認（Swagger UI）

ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs/api-spec && docker compose up -d
```
