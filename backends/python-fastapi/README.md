<p align="center">
<a href="https://www.python.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original-wordmark.svg" height="72" alt="Python"></a>
&nbsp;&nbsp;
<a href="https://fastapi.tiangolo.com/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/fastapi/fastapi-original-wordmark.svg" height="72" alt="FastAPI"></a>
</p>

<p align="center">
<a href="https://www.python.org/downloads/"><img src="https://img.shields.io/badge/Python-3.13-3776AB?logo=python&logoColor=white" alt="Python 3.13"></a>
<a href="https://pypi.org/project/fastapi/"><img src="https://img.shields.io/badge/FastAPI-0.115.12-009688?logo=fastapi&logoColor=white" alt="FastAPI 0.115.12"></a>
<a href="https://pypi.org/project/sqlalchemy/"><img src="https://img.shields.io/badge/SQLAlchemy-2.0.40-D71F00?logo=sqlalchemy&logoColor=white" alt="SQLAlchemy 2.0.40"></a>
</p>

---

## :book: 概要

認可サーバー API の **Python / FastAPI** バックエンド実装です。

Google OAuth 2.0 によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。  
PHP / Laravel 実装と同一 MySQL スキーマを共有し、完全な機能互換を持ちます。  
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

---

## :building_construction: アーキテクチャ

レイヤードアーキテクチャを採用しています。

```
HTTP Request
    │
    ▼
Router (app/routers/)
    │  リクエスト解析・レスポンス整形・FastAPI DI
    ▼
Service (app/services/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    ▼
Repository (app/repositories/)
    │  データアクセス抽象化
    ▼
SQLAlchemy ORM
    │  MySQL ドライバー（PyMySQL）
    ▼
MySQL / Redis
```

### ドメイン一覧

| ドメイン | 責務 |
|---|---|
| **Auth** | Google OAuth 2.0 認証・Cookie セッション管理・招待トークン検証 |
| **Client** | クライアントの CRUD・RSA 4096bit 鍵ペア生成・SSH fingerprint |
| **Gate** | JWT 発行 (`/gate/issue`) と検証 (`/gate/verify`) |
| **Invitation** | スタッフ招待 URL の発行・管理 |
| **Notification** | スタッフへの通知配信・カーソルページネーション・既読管理 |
| **Staff** | スタッフアカウントの管理・ロール変更・論理削除 |

---

## :file_folder: ディレクトリ構成

```
backends/python-fastapi/
├── app/
│   ├── config/             # pydantic-settings による環境変数管理
│   ├── infrastructure/     # DB（SQLAlchemy エンジン）/ Redis クライアント
│   ├── middleware/         # エラーハンドリング
│   ├── models/             # SQLAlchemy ORM モデル（DB スキーマ定義）
│   ├── repositories/       # データアクセス実装
│   ├── routers/            # FastAPI ルーター・依存性注入（DI）
│   ├── services/           # ビジネスロジック実装
│   ├── exceptions.py       # AppError 定義
│   └── main.py             # FastAPI アプリ組み立て・ルーター登録
└── requirements.txt
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `fastapi` | HTTP フレームワーク |
| `uvicorn` | ASGI サーバー |
| `sqlalchemy` | ORM |
| `pymysql` | MySQL ドライバー |
| `python-jose` | JWT 生成・検証（RS256） |
| `redis` | Redis クライアント |
| `cryptography` | RSA 鍵ペア生成・SSH fingerprint |
| `httpx` | Google OAuth 2.0 HTTP クライアント |
| `pydantic-settings` | 環境変数管理 |

---

## :rocket: セットアップ

### 1. 依存パッケージのインストール

```bash
pip install -r requirements.txt
```

### 2. 環境変数の設定

```bash
cp .env.example .env
```

以下を設定してください。

```dotenv
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### 3. 起動

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-python.sh up    # 起動
bin/docker-python.sh down  # 停止
bin/docker-python.sh exec  # コンテナに入る
```
