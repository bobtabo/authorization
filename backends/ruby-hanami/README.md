<p align="center">
<a href="https://www.ruby-lang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ruby/ruby-original.svg" height="72" alt="Ruby"></a>
&nbsp;&nbsp;
<a href="https://hanamirb.org/" target="_blank"><img src="https://avatars.githubusercontent.com/u/3210273" height="72" alt="Hanami"></a>
&nbsp;&nbsp;
<a href="https://rom-rb.org/" target="_blank"><img src="https://rom-rb.org/images/logo--hero.svg" height="72" alt="ROM"></a>
</p>

<p align="center">
<a href="https://www.ruby-lang.org/"><img src="https://img.shields.io/badge/Ruby-latest-CF251F?logo=ruby&logoColor=white" alt="Ruby"></a>
<a href="https://hanamirb.org/"><img src="https://img.shields.io/badge/Hanami-latest-DC350F?logo=hanami&logoColor=white" alt="Hanami"></a>
<a href="https://rom-rb.org/"><img src="https://img.shields.io/badge/ROM-latest-DE0C35?logo=rom&logoColor=white" alt="ROM"></a>
</p>

---

## :book: 概要

認可サーバー API の **Ruby / Hanami** バックエンド実装です。

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
Action (app/actions/)
    │  リクエストのバリデーション・レスポンス整形
    ▼
UseCase / Interactor (app/use_cases/)
    │  アプリケーションロジック・DTO 変換
    ▼
Domain (app/domain/)
    │  エンティティ・リポジトリインターフェース・値オブジェクト
    ▼
Repository (app/infrastructure/repositories/)
    │  ROM リポジトリ実装
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
backends/ruby-hanami/
├── app/
│   ├── actions/             # Hanami Actions（Controller に相当）
│   ├── domain/              # ドメイン層
│   │   ├── client/          # エンティティ・リポジトリ IF・値オブジェクト
│   │   ├── staff/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   ├── use_cases/           # ユースケース層
│   │   ├── client/          # DTO・インタラクター
│   │   ├── staff/
│   │   ├── auth/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   └── infrastructure/      # インフラ層
│       ├── persistence/     # ROM リポジトリ実装
│       └── cache/           # Redis キャッシュリポジトリ実装
├── config/
│   └── routes.rb
├── db/
│   └── migrate/
├── spec/                    # RSpec テスト
├── Gemfile
└── Gemfile.lock
```

---

## :package: 主要 Gem

| Gem | 用途 |
|---|---|
| `hanami` | フレームワーク |
| `rom` | ORM（リポジトリパターン） |
| `rom-sql` | SQL アダプター |
| `jwt` | JWT 生成・検証（RS256） |
| `omniauth-google-oauth2` | Google OAuth 2.0 連携 |
| `redis` | Redis クライアント |
| `mysql2` | MySQL ドライバー |
| `rspec` | テストフレームワーク |

---

## :rocket: セットアップ

### 1. 依存 Gem のインストール

```bash
bundle install
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

### 3. マイグレーション

```bash
bundle exec hanami db migrate
```

### 4. 起動

```bash
bundle exec hanami server
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :test_tube: テスト

```bash
bundle exec rspec
```

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-rb-hanami.sh up    # 起動
bin/docker-rb-hanami.sh down  # 停止
bin/docker-rb-hanami.sh exec  # コンテナに入る
```
