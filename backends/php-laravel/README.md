<p align="center">
<a href="https://www.php.net/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/php/php-original.svg" height="72" alt="PHP"></a>
&nbsp;&nbsp;
<a href="https://laravel.com" target="_blank"><img src="https://raw.githubusercontent.com/laravel/art/master/logo-lockup/5%20SVG/2%20CMYK/1%20Full%20Color/laravel-logolockup-cmyk-red.svg" width="380" alt="Laravel"></a>
</p>

<p align="center">
<a href="https://www.php.net/"><img src="https://img.shields.io/badge/PHP-8.5.4-777BB4?logo=php&logoColor=white" alt="PHP 8.5.4"></a>
<a href="https://packagist.org/packages/laravel/framework"><img src="https://img.shields.io/badge/Laravel-13.4.0-FF2D20?logo=laravel&logoColor=white" alt="Laravel 13.4.0"></a>
</p>

---

## 概要

認可サーバー API の **PHP / Laravel** バックエンド実装です。

Google OAuth 2.0 によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。  
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

---

## アーキテクチャ

ドメイン駆動設計 (DDD) に基づく階層構造を採用しています。

```
HTTP Request
    │
    ▼
Controller (app/Http/Controllers/Api/)
    │  リクエストのバリデーション・レスポンス整形
    ▼
UseCase (app/UseCases/)
    │  アプリケーションロジック・DTO 変換
    ▼
Service (app/Domain/*/Services/)
    │  ドメインロジック
    ▼
Repository Interface (app/Domain/*/Repositories/)
    │  データアクセス抽象化
    ▼
Eloquent Repository (app/Infrastructure/Repositories/)
    │  DB アクセス実装
    ▼
Eloquent Model (app/Infrastructure/Models/)
```

### ドメイン一覧

| ドメイン | 責務 |
|---|---|
| **Auth** | Google OAuth 2.0 認証・セッション管理・招待トークン検証 |
| **Client** | クライアント（連携システム）の CRUD・鍵ペア管理 |
| **Gate** | JWT 発行 (`/gate/issue`) と検証 (`/gate/verify`) |
| **Invitation** | スタッフ招待 URL の発行・管理 |
| **Notification** | スタッフへの通知配信・既読管理 |
| **Staff** | スタッフアカウントの管理・ロール変更 |

---

## ディレクトリ構成

```
app/
├── Domain/             # ドメイン層（エンティティ・サービス・リポジトリ定義）
│   ├── Auth/
│   ├── Client/
│   ├── Gate/
│   ├── Invitation/
│   ├── Notification/
│   └── Staff/
├── Http/
│   ├── Controllers/Api/ # API コントローラー
│   └── Requests/       # フォームリクエスト（バリデーション）
├── Infrastructure/
│   ├── Models/         # Eloquent モデル
│   └── Repositories/   # リポジトリ実装（Eloquent）
├── Support/            # 共通基盤（BaseController・AppRequest 等）
└── UseCases/           # ユースケース層
database/
├── factories/          # テスト用ファクトリー
├── migrations/         # マイグレーション
└── seeders/
tests/
├── Feature/            # Feature テスト
└── TestData/           # テスト用フィクスチャ（Requests/ / Responses/）
bin/                    # 運用スクリプト
routes/
└── api.php             # API ルーティング
```

---

## 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `firebase/php-jwt` | JWT 生成・検証 |
| `laravel/sanctum` | API トークン認証 |
| `laravel/socialite` | Google OAuth 2.0 連携 |
| `mark-gerarts/auto-mapper-plus` | DTO ↔ Entity マッピング |
| `mavinoo/laravel-batch` | Eloquent バッチ UPDATE |

---

## セットアップ

### 1. 依存パッケージのインストール

```bash
composer install
```

### 2. 環境変数の設定

`.env.local` を作成し、以下を設定してください。

```dotenv
APP_ENV=local

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=authorization
DB_USERNAME=root
DB_PASSWORD=secret

REDIS_HOST=host.docker.internal
REDIS_PORT=6379

AUTHORIZATION_FRONTEND_URL=http://localhost:3000

GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8000/auth/google/callback
```

### 3. マイグレーション

```bash
php artisan migrate --seed
```

---

## テスト

| 環境 | コマンド |
|---|---|
| ローカル | `php artisan test --env=testing.local` |
| CI | `php artisan test --env=testing` |

テスト用フィクスチャは `tests/TestData/` に配置します。

```
tests/TestData/
├── Requests/   # リクエストパラメータ（JSON）
└── Responses/  # 期待レスポンス（JSON）
```

---

## ユーティリティスクリプト

| スクリプト | 内容 |
|---|---|
| `bin/clear-laravel-local.sh` | Laravel キャッシュ（config / route / view）一括クリア |
