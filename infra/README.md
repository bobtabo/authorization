# infra/ — LocalStack + Terraform IaC 定義

ローカル開発環境で API Gateway / Lambda を **LocalStack + Terraform** で IaC 管理するための定義です。

---

## 構成

| ファイル | 内容 |
|:---|:---|
| `main.tf` | プロバイダー設定（LocalStack エンドポイント） |
| `variables.tf` | 変数定義（リージョン、ZIP パス、ステージ名等） |
| `lambda.tf` | Lambda 関数 + IAM ロール |
| `apigateway.tf` | API Gateway REST API + ステージ |
| `outputs.tf` | API Gateway URL 等の出力値 |
| `Makefile` | tflocal 操作のショートカット |

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

```bash
# 1. Lambda バイナリをビルド & ZIP 化
cd ../function
make zip

# 2. Terraform でリソースを作成
cd ../infra
make apply

# 3. API Gateway URL を確認
make output
```

### 出力例

```
api_gateway_id       = "abc123def4"
api_gateway_url      = "http://localhost:4566/restapis/abc123def4/local/_user_request_"
lambda_function_name = "authorization-http"
```

---

## リソースの削除

```bash
make destroy
```

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
```
