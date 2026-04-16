<p align="center">
<a href="https://www.rust-lang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rust/rust-original.svg" height="72" alt="Rust"></a>
&nbsp;&nbsp;
<a href="https://github.com/tokio-rs/axum" target="_blank"><img src="https://www.aldeka.net/_app/immutable/assets/ferris.5bb4776d.png" height="72" alt="Gin"></a>
&nbsp;&nbsp;
<a href="https://www.sea-ql.org/SeaORM/" target="_blank"><img src="https://www.sea-ql.org/SeaORM/img/SeaQL.png" height="72" alt="SeaORM"></a>
</p>

<p align="center">
<a href="https://www.rust-lang.org/"><img src="https://img.shields.io/badge/Rust-1.95.0-000000?logo=rust&logoColor=white" alt="Rust 1.95.0"></a>
<a href="https://github.com/tokio-rs/axum"><img src="https://img.shields.io/badge/Axum-0.8.9-F44E00?logo=axum&logoColor=white" alt="Axum 0.8.9"></a>
<a href="https://www.sea-ql.org/SeaORM/"><img src="https://img.shields.io/badge/SeaORM-2.0.0-304ECF?logo=seaorm&logoColor=white" alt="SeaORM 2.0.0"></a>
</p>

---

## :book: 概要

認可サーバー API の **Rust / Axum** バックエンド実装です。

Google OAuth 2.0 によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。  
PHP / Laravel 実装と同一 MySQL スキーマを共有し、完全な機能互換を持ちます。  
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

---

## :building_construction: アーキテクチャ

DDD + クリーンアーキテクチャを採用しています。

```
HTTP Request
    │
    ▼
Handler (internal/handler/)
    │  リクエスト解析・レスポンス整形
    ▼
UseCase / Interactor (internal/usecase/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    │  Domain Repository インターフェースに依存（依存性逆転）
    ▼
Domain (internal/domain/)
    │  エンティティ・リポジトリインターフェース・値オブジェクト
    ▼
Infrastructure (internal/infrastructure/)
    │  GORM 実装リポジトリ・Redis キャッシュ
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
backends/rust-axum/
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|

---

## :rocket: セットアップ

### 1. 依存パッケージの取得

```bash

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

```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-rust.sh up    # 起動
bin/docker-rust.sh down  # 停止
bin/docker-rust.sh exec  # コンテナに入る
```
