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
├── 📂 docker/             # コンテナ定義
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
| **`terraform/`**    | Terraform IaC 定義（環境別）              | [README.md](./terraform/local/README.md) |

---

## :hammer_and_wrench: 開発環境構築手順

### 前提

- Docker Engine / Docker Compose がインストール済みであること
- ポート `443`（プロキシ）、`3306`（MySQL）、`6379`（Redis）、`9000`（Lambda）、`8080`（API Gateway エミュレータ）、`4566`（LocalStack）、`8025`（MailPit）がローカルで空いていること
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

### 3. 共通コンテナの起動（Nginx Proxy / MySQL / Redis / Lambda / LocalStack / MailPit）

```bash
bin/docker-common.sh up
```

### 4. バックエンドコンテナの起動

```bash
bin/docker-backends.sh up
```

### 5. API Gateway エミュレーターの起動

#### 5a. カスタムエミュレーター（従来方式）

```bash
cd function
make run-apigw-emulator
```
> [!NOTE]
>
> HTTP ↔ Lambda イベント変換を担うローカル専用プロセス。</br>Port:8080 で待ち受け、Port:9000 の Lambda コンテナへ転送する。

#### 5b. LocalStack + Terraform（IaC 方式）

LocalStack 上に API Gateway / Lambda を Terraform で構築する方式。</br>
本番 AWS 構成に近い形でローカル検証できる。

```bash
# 1. Lambda 関数を zip にまとめる
cd function
make zip  # → function.zip（bootstrap バイナリ含む）が生成される

# 2. tflocal をインストール（初回のみ）
pip install terraform-local

# 3. Terraform でリソースを作成（完了後に frontend/.env.local が自動生成される）
cd ../terraform/local
make apply
```

> [!NOTE]
>
> LocalStack コンテナが起動済みであること（手順 3 で `docker-common.sh up` 済み）。</br>
> `docker-common.sh up` を実行した場合は、LocalStack 起動後に自動で `tflocal apply` + `.env.local` 生成が行われる。</br>
> `tflocal` は Terraform コマンドを LocalStack エンドポイントに向けるラッパー。</br>
> `make apply` 完了時に `frontend/.env.local` が自動生成される（API Gateway ID 自動解決）。</br>
> 手動で再生成する場合は `cd terraform/local && make setup-env` を実行。

#### 5b-2. ngrok による外部公開（LocalStack 環境）

LocalStack 移行後は、ngrok の接続先を API Gateway エミュレーター（Port:8080）から LocalStack（Port:4566）に変更する。

```yaml
# ~/.config/ngrok/ngrok.yml
tunnels:
  apigw:
    proto: http
    addr: 4566  # 8080 → 4566 に変更
    domain: your-domain.ngrok-free.dev
```

```bash
# ngrok トンネルの起動
ngrok start apigw
```

> [!NOTE]
>
> ngrok 固定ドメインを使用しているため、URL は再起動しても変わらない。</br>
> LocalStack 移行後の URL 形式: `https://{domain}/restapis/{api-id}/{stage}/_user_request_/...`</br>
> `api-id` は `tflocal apply` 実行ごとに変わる可能性があるが、`make apply` 時に `.env.local` へ自動反映される。</br>
> ShowCase CI 等の外部からのアクセスは、このリポジトリ側で対応する（Repository variable 等の外部設定は不要）。

#### 5c. SES メール送信（LocalStack）

Terraform で SES ドメイン認証・送信元アドレスを LocalStack 上に作成します。</br>
`make apply` を実行すれば API Gateway / Lambda と合わせて SES リソースも作成されます。

各バックエンドの `.env` に以下を設定してください:

```bash
AWS_REGION=ap-northeast-1
AWS_ENDPOINT_URL=http://localstack:4566
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
```

> [!NOTE]
>
> メール送信は全バックエンドで AWS SES SDK を使用しています。</br>
> ローカル開発では LocalStack の SES エンドポイントにリクエストが送られます。</br>
> 本番環境では `AWS_ENDPOINT_URL` を空にし、IAM ロールまたはアクセスキーで認証します。

#### 5d. SSM Parameter Store（LocalStack）

Terraform で SSM Parameter Store のパラメータを LocalStack 上に作成します。</br>
`make apply` を実行すれば API Gateway / Lambda / SES と合わせて SSM リソースも作成されます。

```bash
# パラメータ一覧を確認
aws --endpoint-url=http://localhost:4566 ssm get-parameters-by-path \
  --path "/authorization" --recursive --with-decryption
```

管理対象パラメータ:

| パス | 用途 |
|:---|:---|
| `/authorization/database/*` | DB 接続情報（host / port / name / username / password） |
| `/authorization/redis/*` | Redis 接続情報（host / port） |
| `/authorization/oauth/google/*` | Google OAuth クライアント情報 |
| `/authorization/oauth/github/*` | GitHub OAuth クライアント情報 |
| `/authorization/app/*` | アプリケーション共通設定（env / jwt_secret） |

> [!NOTE]
>
> 本番 AWS では SSM Parameter Store で秘密情報を一元管理し、各バックエンドが起動時に取得します。</br>
> ローカル開発では LocalStack の SSM エンドポイントから取得できます。</br>
> `variables.tf` のデフォルト値を変更するか、`terraform.tfvars` で上書きしてください。

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

http://localhost:3000/invitation/8f13761980983d1d9e3950d11b42016f

> [!NOTE]
> 
> このリンクはローカル環境専用です。</br>本番環境では、管理者から発行された招待リンクを使用してください。

### ツール

| ツール | URL |
|:---|:---|
| MailPit（メール確認） | http://localhost:8025/ |
| LocalStack | http://localhost:4566/ |

---

## :rocket: クイックスタート

### 1. API 仕様の確認（Swagger UI）

ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs/api-spec && docker compose up -d
```
