# 認可サーバー

このリポジトリは、OAuth2/OIDC 準拠の認可サーバー構築プロジェクトです。
各コンポーネントの詳細は、それぞれのディレクトリにあるドキュメントを参照してください。

## 🏗️ システム構造 (Directory Architecture)

.
├── 📂 docs/               # 【設計】Single Source of Truth
│   ├── openapi.yml        # API 定義書 (設計図)
│   ├── docker-compose.yml # 閲覧環境 (Swagger UI)
│   └── .env               # ポート管理設定
├── 📂 backend/            # 【実装】Laravel 11 Core (Now Building...)
│   └── ...                # Passport / Sanctum による認可ロジック
└── 📜 README.md           # プロジェクト全体ガイド

## 📂 プロジェクト構成

| ディレクトリ | 内容 | ドキュメント |
| :--- | :--- | :--- |
| **`docs/`** | API仕様書 (OpenAPI) & Swagger UI 環境 | [README.md](./docs/README.md) |
| **`backend/`** | Laravel 11 による認可サーバー本体 (API) | [README.md](./backend/README.md) |
| **`frontend/`** | ユーザー認可画面・管理画面 (Next.js 等) | [README.md](./frontend/README.md) |

---

## 🚀 クイックスタート

### 1. API仕様の確認 (Swagger UI)
ローカルで OpenAPI エディタと UI を起動します。

```bash
cd docs
docker compose up -d
```