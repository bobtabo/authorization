# <img src="https://cdn.jsdelivr.net/npm/lucide-static@latest/icons/shield-check.svg" height="32" style="margin-top:-4px;vertical-align:middle;" alt="認可サーバー"> 認可サーバー

このリポジトリは、OAuth2/OIDC 準拠の認可サーバー構築プロジェクトです。  
各コンポーネントの詳細は、それぞれのディレクトリにあるドキュメントを参照してください。

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

## :rocket: クイックスタート

### 1. API 仕様の確認（Swagger UI）

ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs/api-spec && docker compose up -d
```
