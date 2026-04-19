<p align="center">
<a href="https://kotlinlang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg" height="72" alt="Kotlin"></a>
&nbsp;&nbsp;
<a href="https://ktor.io/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ktor/ktor-original.svg" height="72" alt="Ktor"></a>
&nbsp;&nbsp;
<a href="https://www.jetbrains.com/exposed/" target="_blank"><img src="https://resources.jetbrains.com/help/img/exposed/exposed-icon.svg" height="72" alt="Exposed"></a>
</p>

<p align="center">
<a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-latest-C50FDF?logo=kotlin&logoColor=white" alt="Kotlin"></a>
<a href="https://ktor.io/"><img src="https://img.shields.io/badge/Ktor-latest-F27D2C?logo=ktor&logoColor=white" alt="Ktor"></a>
<a href="https://www.jetbrains.com/exposed/"><img src="https://img.shields.io/badge/Exposed-latest-E945FA?logo=exposed&logoColor=white" alt="Exposed"></a>
</p>

---

## :book: 概要

認可サーバー API の **Kotlin / Ktor** バックエンド実装です。

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
Handler (src/main/kotlin/.../handler/)
    │  リクエスト解析・レスポンス整形
    ▼
UseCase / Interactor (.../usecase/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    │  Domain Repository インターフェースに依存（依存性逆転）
    ▼
Domain (.../domain/)
    │  エンティティ・リポジトリインターフェース・値オブジェクト
    ▼
Infrastructure (.../infrastructure/)
    │  Exposed リポジトリ・Redis キャッシュ
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
backends/kotlin-ktor/
├── src/main/kotlin/com/authorization/
│   ├── Application.kt       # エントリーポイント・DI 組み立て・ルーティング
│   ├── config/              # 環境変数読み込み
│   ├── domain/              # ドメイン層
│   │   ├── client/          # エンティティ・リポジトリ IF・値オブジェクト
│   │   ├── staff/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   ├── usecase/             # ユースケース層
│   │   ├── client/          # DTO・インタラクター
│   │   ├── staff/
│   │   ├── auth/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   ├── infrastructure/      # インフラ層
│   │   ├── model/           # Exposed テーブル定義
│   │   ├── persistence/     # Exposed リポジトリ実装
│   │   ├── cache/           # Redis キャッシュリポジトリ実装
│   │   └── db/              # DB 接続
│   ├── handler/             # Ktor ハンドラー層
│   └── middleware/          # 認証・エラーハンドリングミドルウェア
├── src/test/kotlin/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `ktor-server` | HTTP フレームワーク |
| `exposed` | ORM |
| `mysql-connector-java` | MySQL ドライバー |
| `lettuce` | Redis クライアント |
| `java-jwt` | JWT 生成・検証（RS256） |
| `ktor-server-sessions` | Cookie セッション管理 |
| `logback` | ロギング |

---

## :rocket: セットアップ

### 1. 依存パッケージのビルド

```bash
./gradlew build
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
./gradlew run
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :test_tube: テスト

```bash
./gradlew test
```

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-kotlin.sh up    # 起動
bin/docker-kotlin.sh down  # 停止
bin/docker-kotlin.sh exec  # コンテナに入る
```
