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
  - [5. LocalStack デプロイ（API Gateway / Lambda）](#5-localstack-デプロイapi-gateway--lambda)
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

### 1. リポジトリのクローン

```bash
git clone git@github.com:bobtabo/authorization.git
cd authorization
```

### 2. 初回セットアップ

```bash
cd docker
find ./bin -type f -exec chmod 755 {} +
# 証明書・環境変数の配置
bin/docker-common.sh env

# docker/local/common/.env を編集して LOCALSTACK_AUTH_TOKEN を設定する
# トークンは https://app.localstack.cloud/ から取得
vi local/common/.env
# LOCALSTACK_AUTH_TOKEN=ls-xxxx...
```

### 3. 共通コンテナの起動

`BACKEND_MODE` に応じて起動するサービスが切り替わります。

| モード | 環境 | 費用 | 状態 |
|:---|:---|:---|:---|
| `localstack`（デフォルト）| LocalStack Community | 無料 | **推奨** |
| `localstack-pro` | LocalStack Pro | 有料 | 将来対応 |
| `emulator` | Lambda 常駐 + MailPit | 無料 | 非推奨（LocalStack が使えない環境向け）|

```bash
# デフォルト（localstack モード）で起動
bin/docker-common.sh up

# モードを切り替える場合は .env の BACKEND_MODE を変更してから実行
# vi docker/local/common/.env
# BACKEND_MODE=emulator
```

> [!NOTE]
>
> `docker-common.sh up` は `BACKEND_MODE` に応じた Docker Compose ファイルを自動選択します。</br>
> **localstack / localstack-pro**: `docker-compose.yml` + `docker-compose.localstack[|-pro].yml` で起動後、`docker-localstack-init.sh` が自動実行され、**`make zip` → `make apply`（Terraform デプロイ）→ `frontend/.env.local` 生成まで自動完了**します。</br>
> **emulator**: `docker-compose.yml` + `docker-compose.emulator.yml` で起動します（非推奨）。</br>
> フロントエンド用の `.env` もモードに合わせて切り替えてください（`.env.localstack` / `.env.emulator`）。</br>
> `down` / `stop` を実行する際は、`BACKEND_MODE` を起動時と同じ値にしてください。異なるモードで実行すると対象コンテナが正しく停止されません。

#### 補足: LocalStack を再デプロイしたい場合

`docker-common.sh down` 後の再起動や、Terraform 定義を変更した場合など、手動で再デプロイが必要な場合のみ以下を実行してください。通常は `docker-common.sh up` で自動完了するため手動実行は不要です。

```bash
# 1. Lambda 関数を zip にまとめる
cd function
make zip  # → function.zip（bootstrap バイナリ含む）が生成される

# 2. Terraform でリソースを作成（完了後に frontend/.env.local が自動生成される）
cd ../terraform/local
make apply
```

> [!NOTE]
>
> `tflocal` は Terraform コマンドを LocalStack エンドポイントに向けるラッパー。</br>
> `make apply` 完了時に `frontend/.env.local` が自動生成される（API Gateway ID 自動解決）。</br>
> 手動で再生成する場合は `cd terraform/local && make setup-env` を実行。

> [!TIP]
>
> **emulator モード（非推奨）** を使用する場合は、カスタム API Gateway エミュレーターを手動起動します:
> ```bash
> cd function
> make run-apigw-emulator
> ```
> HTTP ↔ Lambda イベント変換を担うローカル専用プロセス。Port:8080 で待ち受け、Port:9000 の Lambda コンテナへ転送します。</br>
> LocalStack が使えない環境でのみ使用してください。

### 4. バックエンドコンテナの起動

```bash
bin/docker-backends.sh up
```

#### ngrok による外部公開

ngrok の接続先を LocalStack（Port:4566）に設定する。

```yaml
# ~/.config/ngrok/ngrok.yml
tunnels:
  apigw:
    proto: http
    addr: 4566
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

#### SES メール送信（LocalStack）

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

#### SSM Parameter Store（LocalStack）

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

### 5. フロントエンドの起動

```bash
cd frontend
npm install
npm run dev
```

### 6. バックエンドの初期設定

各バックエンドで環境変数の設定が必要です。</br>
使用するバックエンドのみ実施してください。

#### 6.1 PHP（Laravel）

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

#### 6.2 Go（Gin）

```bash
bin/docker-go-gin.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.3 Go（Beego）

```bash
bin/docker-go-beego.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.4 Go（Echo）

```bash
bin/docker-go-echo.sh exec
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.5 Kotlin（Ktor）

```bash
bin/docker-kotlin.sh exec
gradle build
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.6 Python（FastAPI）

```bash
bin/docker-python.sh exec
pip install -r requirements.txt
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.7 TypeScript（Hono）

```bash
bin/docker-ts.sh exec
npm install
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.8 Ruby（Rails）

```bash
bin/docker-rb-rails.sh exec
bundle install
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.9 Ruby（Hanami）

```bash
bin/docker-rb-hanami.sh exec
bundle install
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

#### 6.10 Rust（Axum）

```bash
bin/docker-rust.sh exec
cargo build
cp .env.example .env
# .env の GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET を設定する
```

### 7. 初回ログイン

初回は招待リンクからアクセスする必要があります。</br>
招待リンクなしでは、ログイン画面からサインインできません。

http://localhost:3000/invitation/8f13761980983d1d9e3950d11b42016f

> [!NOTE]
> 
> このリンクはローカル環境専用です。</br>本番環境では、管理者から発行された招待リンクを使用してください。

### ツール

| ツール | URL |
|:---|:---|
| LocalStack Web UI | http://localhost:4566/ |

---

## :gear: BACKEND_MODE

`BACKEND_MODE` 環境変数で、バックエンドのインフラ構成を切り替えます。

| モード | 構成 | 費用 | 状態 |
|:---|:---|:---|:---|
| `localstack`（デフォルト）| LocalStack Community（API Gateway + Lambda + SES + SSM）| 無料 | **推奨** |
| `localstack-pro` | LocalStack Pro | 有料 | 将来対応 |
| `emulator` | カスタム API Gateway エミュレーター + Lambda 常駐 + MailPit | 無料 | 非推奨 |

### 切り替え方法

```bash
# docker/local/common/.env の BACKEND_MODE を変更
vi docker/local/common/.env
# BACKEND_MODE=localstack  ← デフォルト（推奨）
# BACKEND_MODE=emulator    ← 非推奨

# 変更後にコンテナを再起動
cd docker
bin/docker-common.sh up
```

### フロントエンド環境変数

| モード | 環境変数ファイル |
|:---|:---|
| `localstack` | `frontend/.env.local`（`make apply` 時に自動生成） |
| `emulator` | `frontend/.env.local`（`cp .env.emulator .env.local`） |

---

## :rocket: クイックスタート

### 1. API 仕様の確認（Swagger UI）

ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs/api-spec && docker compose up -d
```
