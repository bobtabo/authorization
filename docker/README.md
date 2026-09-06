<p align="center">
<a href="https://www.docker.com/" target="_blank"><img src="https://findy-tools.io/public_images/tool_vendor/docker/logo_docker_square.png.png" height="72" alt="Docker"></a>
&nbsp;&nbsp;
<a href="https://nginx.org/" target="_blank"><img src="https://images.icon-icons.com/2699/PNG/512/nginx_logo_icon_169915.png" height="72" alt="nginx"></a>
&nbsp;&nbsp;
<a href="https://www.mysql.com/" target="_blank"><img src="https://cdn.cdnlogo.com/logos/m/10/mysql.svg" height="72" alt="MySQL"></a>
&nbsp;&nbsp;
<a href="https://redis.io/" target="_blank"><img src="https://media.ffycdn.net/us/redis/MAQLWqeBKmrz2TFQDmA7.svg" height="72" alt="Redis"></a>
&nbsp;&nbsp;
<a href="https://localstack.cloud/" target="_blank"><img src="https://avatars.githubusercontent.com/u/28732122?s=200&v=4" height="72" alt="LocalStack"></a>
</p>

<p align="center">
<a href="https://www.docker.com/"><img src="https://img.shields.io/badge/Docker-latest-1D63ED?logo=docker&logoColor=white" alt="Docker"></a>
<a href="https://nginx.org/"><img src="https://img.shields.io/badge/nginx_proxy-latest-009639?logo=nginx&logoColor=white" alt="nginx proxy"></a>
<a href="https://www.mysql.com/"><img src="https://img.shields.io/badge/MySQL-8.0-00758F?logo=mysql&logoColor=white" alt="MySQL 8.0"></a>
<a href="https://redis.io/"><img src="https://img.shields.io/badge/Redis-7.0-FF4438?logo=redis&logoColor=white" alt="Redis 7.0"></a>
<a href="https://localstack.cloud/"><img src="https://img.shields.io/badge/LocalStack-latest-4728E3?logoColor=white" alt="LocalStack"></a>
</p>

---

## :file_folder: ディレクトリ構成

| パス                                                                   | 内容                                                                 |
|----------------------------------------------------------------------|--------------------------------------------------------------------|
| [`develop/`](./develop/)                                             | AWSの開発環境用を想定                                                       |
| [`local/app-go-gin/`](local/app-go-gin/)     | Go（Gin）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。                  |
| [`local/app-go-echo/`](local/app-go-echo/)   | Go（Echo）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。                 |
| [`local/app-go-beego/`](local/app-go-beego/) | Go（Beego）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。                |
| [`local/app-csharp/`](local/app-csharp/)     | C#（ASP.NET Core）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。         |
| [`local/app-kotlin/`](local/app-kotlin/)     | Kotlin（Ktor）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。             |
| [`local/app-php/`](local/app-php/)           | PHP 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。                    |
| [`local/app-python/`](local/app-python/)     | Python 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。                 |
| [`local/app-rb-hanami/`](local/app-rb-hanami/) | Ruby（Hanami）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。           |
| [`local/app-rb-rails/`](local/app-rb-rails/)   | Ruby（Rails）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。            |
| [`local/app-rust/`](local/app-rust/)         | Rust（Axum）実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。             |
| [`local/app-ts/`](local/app-ts/)             | TypeScript 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける。             |
| [`local/common/`](local/common/)             | 複数バックエンドで共有する共通インフラ。                                              |
| [`production/`](./production/)                                       | AWSの本番環境用を想定                                                       |
| [`staging/`](./staging/)                                             | AWSの検証環境用を想定                                                       |

`common` 側で Docker ネットワーク `authorization` を作成し、各 `docker-compose` はそのネットワークに参加します（`external: true`）。

## :white_check_mark: 前提

- Docker Engine および Docker Compose v2（`docker compose` サブコマンド）が使えること。v1 の `docker-compose` は非対応
- ポート **443**（プロキシ）、**3306**（MySQL）、**6379**（Redis）などがローカルで空いていること（`.env` で変更可）

## :whale: 共通コンテナ操作

`BACKEND_MODE`（`docker/local/common/.env`）に応じて、起動する Docker Compose ファイルが自動選択されます。

| モード | 起動ファイル | 補足 |
|:---|:---|:---|
| `localstack`（デフォルト）| `docker-compose.yml` + `docker-compose.localstack.yml` | 推奨 |
| `localstack-pro` | `docker-compose.yml` + `docker-compose.localstack-pro.yml` | 将来対応 |
| `emulator` | `docker-compose.yml` + `docker-compose.emulator.yml` | 非推奨 |

### 事前準備
```bash
cd docker

# 初回のみ: スクリプトに実行権限
find ./bin -type f -exec chmod 755 {} +
# 証明書・環境変数の配置
bin/docker-common.sh env

# docker/local/common/.env を編集して LOCALSTACK_AUTH_TOKEN を設定する
# トークンは https://app.localstack.cloud/ から取得
vi local/common/.env
# LOCALSTACK_AUTH_TOKEN=ls-xxxx...
```

### コンテナを起動する
```bash
# 起動（内部で authorization ネットワーク作成 + compose up）
bin/docker-common.sh up
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

### コンテナを停止する
```bash
bin/docker-common.sh stop
```

### コンテナを再開する
```bash
bin/docker-common.sh start
```

### コンテナを破棄する
```bash
# ボリュームや data も消えるので注意！
bin/docker-common.sh down
```

## :gear: アプリコンテナ操作

`common` でネットワークとプロキシが立ち上がった状態で、各アプリ環境を起動します。

### コンテナを起動する

```bash
# Go（Gin）環境を起動する
bin/docker-go-gin.sh up

# Go（Echo）環境を起動する
bin/docker-go-echo.sh up

# Go（Beego）環境を起動する
bin/docker-go-beego.sh up

# C#（ASP.NET Core）環境を起動する
bin/docker-csharp.sh up

# Kotlin（Ktor）環境を起動する
bin/docker-kotlin.sh up

# PHP環境を起動する
bin/docker-php.sh up

# Python環境を起動する
bin/docker-python.sh up

# Ruby（Hanami）環境を起動する
bin/docker-rb-hanami.sh up

# Ruby（Rails）環境を起動する
bin/docker-rb-rails.sh up

# Rust（Axum）環境を起動する
bin/docker-rust.sh up

# TypeScript環境を起動する
bin/docker-ts.sh up
```

### コンテナに入る

```bash
# Go（Gin）環境に入る
bin/docker-go-gin.sh exec

# Go（Echo）環境に入る
bin/docker-go-echo.sh exec

# Go（Beego）環境に入る
bin/docker-go-beego.sh exec

# C#（ASP.NET Core）環境に入る
bin/docker-csharp.sh exec

# Kotlin（Ktor）環境に入る
bin/docker-kotlin.sh exec

# PHP環境に入る
bin/docker-php.sh exec

# Python環境に入る
bin/docker-python.sh exec

# Ruby（Hanami）環境に入る
bin/docker-rb-hanami.sh exec

# Ruby（Rails）環境に入る
bin/docker-rb-rails.sh exec

# Rust（Axum）環境に入る
bin/docker-rust.sh exec

# TypeScript環境に入る
bin/docker-ts.sh exec
```

### コンテナを破棄する

```bash
# Go（Gin）環境を破棄する
bin/docker-go-gin.sh down

# Go（Echo）環境を破棄する
bin/docker-go-echo.sh down

# Go（Beego）環境を破棄する
bin/docker-go-beego.sh down

# C#（ASP.NET Core）環境を破棄する
bin/docker-csharp.sh down

# Kotlin（Ktor）環境を破棄する
bin/docker-kotlin.sh down

# PHP環境を破棄する
bin/docker-php.sh down

# Python環境を破棄する
bin/docker-python.sh down

# Ruby（Hanami）環境を破棄する
bin/docker-rb-hanami.sh down

# Ruby（Rails）環境を破棄する
bin/docker-rb-rails.sh down

# Rust（Axum）環境を破棄する
bin/docker-rust.sh down

# TypeScript環境を破棄する
bin/docker-ts.sh down
```

### 全コンテナを一括起動する

```bash
bin/docker-backends.sh up
```

### 全コンテナを一括破棄する

```bash
bin/docker-backends.sh down
```

## :fire: 注意

- `docker-xxx-down.sh` は **データディレクトリやログを削除する**処理が入っています。実行前に内容を確認してください。
- 証明書・パスワード類は **開発用サンプル**です。共有環境では流用しないでください。

## :bulb: 各ツール

| ツール | URL | 備考 |
|:---|:---|:---|
| LocalStack ヘルスチェック | http://localhost:4566/_localstack/health | Community 版で利用可 |
| LocalStack Web UI | http://localhost:4566/ | Pro 版のみ |
| MailPit（メール確認） | http://localhost:8025/ | localstack モードで利用可 |

