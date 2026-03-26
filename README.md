# 認可サーバー

このリポジトリは、OAuth2/OIDC 準拠の認可サーバー構築プロジェクトです。
各コンポーネントの詳細は、それぞれのディレクトリにあるドキュメントを参照してください。

## 🏗️ システム構造 (Directory Architecture)

```.
├── 📂 backends/           # (まだ空でOK) ここに backend を入れる
├── 📂 bin/                # (まだ空でOK) ここに 便利スクリプト を入れる
├── 📂 docker/             # コンテナ定義
├── 📂 docs/               # Swagger 用 Docker 設定
│   ├── api-spec/          # API ドキュメントルート (OpenAPI / Swagger UI)
│   │   ├── openapi.yml
│   │   ├── docker-compose.yml
│   │   └── .env
│   └── ui-flow/           # 画面フロー
│       └── src
├── 📂 frontend/           # (まだ空でOK) ここに frontend を入れる
├── 📂 function/           # AWS Lambda 関数 (Go)
└── 📜 README.md           # これはプロジェクトの「ルート」に置く
```

## 📂 プロジェクト構成

| ディレクトリ              | 内容                               | ドキュメント                                 |
|:--------------------|:---------------------------------|:---------------------------------------|
| **`backends/`**     | 認可サーバー本体 (API)                   | [README.md](./backend/README.md)       |
| **`bin/`**          | 便利スクリプト                          | [README.md](./bin/README.md)           |
| **`docker/`**       | コンテナ定義                           | [README.md](./docker/README.md)        |
| **`docs/api-spec`** | API仕様書 (OpenAPI) & Swagger UI 環境 | [README.md](./docs/api-spec/README.md) |
| **`docs/ui-flow`**  | 画面フロー                            | [README.md](./docs/ui-flow/README.md)  |
| **`frontend/`**     | 認可管理画面 (Next.js 等)               | [README.md](./frontend/README.md)      |
| **`function/`**     | AWS Lambda 関数 (Go)               | [README.md](./function/README.md)      |

---

## 🚀 クイックスタート

### 1. API仕様の確認 (Swagger UI)
ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs && docker compose up -d
```