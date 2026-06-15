# terraform/local/ — LocalStack + Terraform IaC 定義

ローカル開発環境で API Gateway / Lambda / SES / SSM Parameter Store を **LocalStack + Terraform** で IaC 管理するための定義です。

---

## 構成

| ファイル | 内容 |
|:---|:---|
| `main.tf` | プロバイダー設定（LocalStack エンドポイント） |
| `variables.tf` | 変数定義（リージョン、ZIP パス、ステージ名等） |
| `lambda.tf` | Lambda 関数 + IAM ロール |
| `apigateway.tf` | API Gateway REST API + ステージ |
| `ses.tf` | SES ドメイン認証・送信元アドレス |
| `ssm.tf` | SSM Parameter Store（DB / Redis / OAuth / アプリ設定） |
| `outputs.tf` | API Gateway URL 等の出力値 |
| `Makefile` | tflocal 操作のショートカット |
| `scripts/setup-env.sh` | API Gateway ID を取得し `frontend/.env.local` を自動生成 |

---

## 前提

- Docker Compose で共通コンテナ（LocalStack 含む）が起動済みであること
- `terraform` コマンドがインストール済みであること
- `tflocal`（terraform-local）がインストール済みであること

```bash
pip install terraform-local
```

---

## 使い方

> [!IMPORTANT]
> 通常は `bin/docker-common.sh up` を実行すれば、以下の手順が自動で行われます。
> 再適用が必要な場合のみ手動で実行してください。
> その場合は先に LocalStack が起動済みであること（`docker ps` で確認）を確認してください。

```bash
# 1. Lambda バイナリをビルド & ZIP 化
cd ../../function
make zip

# 2. Terraform でリソースを作成（完了後に frontend/.env.local が自動生成される）
cd ../terraform/local
make apply

# 3. API Gateway URL を確認
make output
```

> [!NOTE]
>
> `make apply` 完了時に `scripts/setup-env.sh` が自動実行され、</br>
> `frontend/.env.localstack` をテンプレートとして `frontend/.env.local` が生成される。</br>
> `.env.local` の再生成のみ行いたい場合は `make setup-env` を実行する。

### 出力例

```
api_gateway_id       = "abc123def4"
api_gateway_url      = "http://localhost:4566/restapis/abc123def4/local/_user_request_"
lambda_function_name = "authorization-http"
ses_domain           = "example.com"
ses_from_address     = "no-reply@example.com"
ssm_parameter_prefix = "/authorization"
ssm_parameter_paths  = [
  "/authorization/database/host",
  "/authorization/database/port",
  "/authorization/database/name",
  "/authorization/database/username",
  "/authorization/database/password",
  "/authorization/redis/host",
  "/authorization/redis/port",
  "/authorization/oauth/google/client_id",
  "/authorization/oauth/google/client_secret",
  "/authorization/oauth/github/client_id",
  "/authorization/oauth/github/client_secret",
  "/authorization/app/env",
  "/authorization/app/jwt_secret",
]
```

---

## SSM Parameter Store

`ssm.tf` で以下のパラメータを管理しています:

| パス | タイプ | 説明 |
|:---|:---|:---|
| `/authorization/database/host` | String | DB ホスト |
| `/authorization/database/port` | String | DB ポート |
| `/authorization/database/name` | String | DB 名 |
| `/authorization/database/username` | SecureString | DB ユーザー名 |
| `/authorization/database/password` | SecureString | DB パスワード |
| `/authorization/redis/host` | String | Redis ホスト |
| `/authorization/redis/port` | String | Redis ポート |
| `/authorization/oauth/google/client_id` | SecureString | Google OAuth Client ID |
| `/authorization/oauth/google/client_secret` | SecureString | Google OAuth Client Secret |
| `/authorization/oauth/github/client_id` | SecureString | GitHub OAuth Client ID |
| `/authorization/oauth/github/client_secret` | SecureString | GitHub OAuth Client Secret |
| `/authorization/app/env` | String | アプリケーション環境名 |
| `/authorization/app/jwt_secret` | SecureString | JWT シークレット |

### 実際の値の設定

`variables.tf` のデフォルト値はプレースホルダーです。</br>
ローカル開発で実際の値を使用する場合は `terraform/local/terraform.tfvars` を作成してください（`.gitignore` 対象）。

```hcl
# terraform/local/terraform.tfvars
ssm_db_username        = "develop"
ssm_db_password        = "docker#DOCKER1234"
ssm_google_client_id   = "your-real-google-client-id"
ssm_google_client_secret = "your-real-google-client-secret"
ssm_github_client_id   = "your-real-github-client-id"
ssm_github_client_secret = "your-real-github-client-secret"
ssm_app_jwt_secret     = "your-real-jwt-secret"
```

### パラメータの取得（AWS CLI）

```bash
# 全パラメータを一覧表示
aws --endpoint-url=http://localhost:4566 ssm get-parameters-by-path \
  --path "/authorization" --recursive --with-decryption

# 個別パラメータの取得
aws --endpoint-url=http://localhost:4566 ssm get-parameter \
  --name "/authorization/database/host"

# SecureString の復号取得
aws --endpoint-url=http://localhost:4566 ssm get-parameter \
  --name "/authorization/database/password" --with-decryption
```

---

## リソースの削除

```bash
make destroy
```

> [!TIP]
> LocalStack 再起動後は全リソースが消えるため、残存する `terraform.tfstate` と実態が食い違う場合があります。
> `make apply` を再実行すれば自動的に再作成されますが、エラーが出た場合は `make clean` で tfstate を削除してから再試行してください。

---

## 構成図

```
ブラウザ
↓
フロントエンド（localhost:3000）
↓
API Gateway（LocalStack: localhost:4566）
↓
Lambda（LocalStack 管理）
↓
Nginx Proxy（既存 Docker コンテナ）
↓
各バックエンド（既存 Docker コンテナ）
  ↓
  SSM Parameter Store（LocalStack: localhost:4566）
  — DB / Redis / OAuth 等の秘密情報を取得
```
