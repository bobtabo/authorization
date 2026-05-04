# <img src="https://cdn.jsdelivr.net/npm/lucide-static@latest/icons/shield-check.svg" height="32" style="margin-top:-4px;vertical-align:middle;" alt="認可サーバー"> 認可サーバー

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
  - [3. 共通コンテナの起動](#3-共通コンテナの起動nginx-proxy--mysql--redis--lambda--mailpit)
  - [4. バックエンドコンテナの起動](#4-バックエンドコンテナの起動)
  - [5. API Gateway エミュレーターの起動](#5-api-gateway-エミュレーターの起動)
  - [6. フロントエンドの起動](#6-フロントエンドの起動)
  - [7. バックエンドの初期設定](#7-バックエンドの初期設定)
    - [7.1 PHP（Laravel）](#71-phplaravel)
    - [7.2 Go（Gin）](#72-gogin)
    - [7.3 Go（Beego）](#73-gobeego)
    - [7.4 Go（Echo）](#74-goecho)
    - [7.5 Kotlin（Ktor）](#75-kotlinktor)
    - [7.6 Python（FastAPI）](#76-pythonfastapi)
    - [7.7 TypeScript（Hono）](#77-typescripthono)
    - [7.8 Ruby（Rails）](#78-rubyrails)
    - [7.9 Ruby（Hanami）](#79-rubyhanami)
    - [7.10 Rust（Axum）](#710-rustaxum)
  - [8. 初回ログイン](#8-初回ログイン)
- [クイックスタート](#クイックスタート)

---

## :building_construction: システム構造

```.
├── 📂 backends/           # バックエンド構成（go-gin / php-laravel / python-fastapi / ts-hono）
├── 📂 docker/             # コンテナ定義
├── 📂 docs/
│   ├── api-spec/          # API 仕様書（OpenAPI / Swagger UI）
│   └── ui-flow/           # 画面フロー
├── 📂 frontend/           # 認可管理画面（React / Next.js）
├── 📂 function/           # AWS Lambda 関数（Go）
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

---

## :hammer_and_wrench: 開発環境構築手順

### 前提

- Docker Engine / Docker Compose がインストール済みであること
- ポート `443`（プロキシ）、`3306`（MySQL）、`6379`（Redis）、`9000`（Lambda）、`8080`（API Gateway エミュレータ）、`8025`（MailPit）がローカルで空いていること
- Google OAuth 2.0 のクライアント ID / シークレットを取得済みであること（<a href="https://console.cloud.google.com/">Google Cloud Console</a>）
- GitHub OAuth App のクライアント ID / シークレットを取得済みであること（<a href="https://github.com/settings/developers">GitHub Developer Settings</a>）

### 1. リポジトリのクローン

```bash
git clone git@github.com:bobtabo/authorization.git
cd authorization
```

### 2. 初回セットアップ

```bash
cd docker
find ./bin -type f -exec chmod 755 {} +
bin/docker-environment.sh
```

### 3. 共通コンテナの起動（Nginx Proxy / MySQL / Redis / Lambda / MailPit）

```bash
bin/docker-common.sh up
```

### 4. バックエンドコンテナの起動

```bash
bin/docker-backends.sh up
```

### 5. API Gateway エミュレーターの起動

```bash
cd function
make run-apigw-emulator
```
> [!NOTE]
>
> HTTP ↔ Lambda イベント変換を担うローカル専用プロセス。</br>Port:8080 で待ち受け、Port:9000 の Lambda コンテナへ転送する。

### 6. フロントエンドの起動

```bash
cd frontend
npm install
npm run dev
```

### 7. バックエンドの初期設定

各バックエンドで環境変数の設定が必要です。</br>
使用するバックエンドのみ実施してください。

#### 7.1 PHP（Laravel）

```bash
# コンテナに入る
bin/docker-php.sh exec

# パッケージインストール
composer install

# 環境変数の設定
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する

# マイグレーション
php artisan migrate --seed
```

> [!NOTE]
> 
> マイグレーションは PHP（Laravel）に一本化している。</br>他のバックエンドはテスト用のスキーマ定義を個別に持つ。

#### 7.2 Go（Gin）

```bash
bin/docker-go-gin.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.3 Go（Beego）

```bash
bin/docker-go-beego.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.4 Go（Echo）

```bash
bin/docker-go-echo.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.5 Kotlin（Ktor）

```bash
bin/docker-kotlin.sh exec
gradle build
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.6 Python（FastAPI）

```bash
bin/docker-python.sh exec
pip install -r requirements.txt
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.7 TypeScript（Hono）

```bash
bin/docker-ts.sh exec
npm install
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.8 Ruby（Rails）

```bash
bin/docker-rb-rails.sh exec
bundle install
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.9 Ruby（Hanami）

```bash
bin/docker-rb-hanami.sh exec
bundle install
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 7.10 Rust（Axum）

```bash
bin/docker-rust.sh exec
cargo build
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

### 8. 初回ログイン

初回は招待リンクからアクセスする必要があります。</br>
招待リンクなしでは、ログイン画面からサインインできません。

http://localhost:3000/invitation/b9195889-36c7-631e-76ab-867fa6ad42dc

> [!NOTE]
> 
> このリンクはローカル環境専用です。</br>本番環境では、管理者から発行された招待リンクを使用してください。

### ツール

| ツール | URL |
|:---|:---|
| MailPit（メール確認） | http://localhost:8025/ |

---

## :rocket: クイックスタート

### 1. API 仕様の確認（Swagger UI）

ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs/api-spec && docker compose up -d
```
