<p align="center">
<a href="https://openjdk.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg" height="72" alt="Java"></a>
&nbsp;&nbsp;
<a href="https://spring.io/projects/spring-boot" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg" height="72" alt="Spring Boot"></a>
&nbsp;&nbsp;
<a href="https://www.jooq.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/gradle/gradle-original.svg" height="72" alt="Gradle"></a>
</p>

<p align="center">
<a href="https://aws.amazon.com/corretto/"><img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21 (Amazon Corretto)"></a>
<a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.1"></a>
<a href="https://www.jooq.org/"><img src="https://img.shields.io/badge/jOOQ-3.21.5-176DEA?logoColor=white" alt="jOOQ 3.21.5"></a>
</p>

---

## :book: 概要

認可サーバー API の **Java / Spring Boot** バックエンド実装です。

Google / GitHub OAuth によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。
PHP / Laravel 実装と同一 MySQL スキーマを共有し、完全な機能互換を持ちます。
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

ビルドは Gradle（Kotlin DSL）、ランタイムは Amazon Corretto（JDK 21）を使用します。

---

## :building_construction: アーキテクチャ

DDD + クリーンアーキテクチャを採用しています。

```
HTTP Request
    │
    ▼
Controller (src/main/java/.../http/controllers/)
    │  リクエスト解析・レスポンス整形
    ▼
UseCase / Service (.../usecases/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    │  Domain Repository インターフェースに依存（依存性逆転）
    ▼
Domain (.../domain/)
    │  エンティティ・リポジトリインターフェース・値オブジェクト
    ▼
Infrastructure (.../infrastructure/)
    │  jOOQ リポジトリ実装・Redis キャッシュ
    ▼
MySQL / Redis
```

### ドメイン一覧

| ドメイン | 責務 |
|---|---|
| **Auth** | Google / GitHub OAuth 認証・Cookie セッション管理・招待トークン検証 |
| **Client** | クライアントの CRUD・RSA 4096bit 鍵ペア生成・fingerprint |
| **Gate** | JWT 発行 (`/gate/issue`) と検証 (`/gate/verify`)・発行履歴 |
| **Invitation** | スタッフ招待 URL の発行・ローテーション・管理 |
| **Notification** | スタッフへの通知配信・カーソルページネーション・既読管理 |
| **Staff** | スタッフアカウントの管理・ロール変更・論理削除 |

---

## :file_folder: ディレクトリ構成

```
backends/java-spring/
├── src/main/java/com/authorization/
│   ├── Application.java     # エントリーポイント
│   ├── config/               # 環境変数読み込み・DI 設定
│   ├── domain/                # ドメイン層
│   │   ├── client/            # エンティティ・リポジトリ IF・値オブジェクト
│   │   ├── staff/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   ├── usecases/              # ユースケース層
│   │   ├── client/            # DTO・Service
│   │   ├── staff/
│   │   ├── auth/
│   │   ├── invitation/
│   │   ├── notification/
│   │   └── gate/
│   ├── infrastructure/        # インフラ層
│   │   ├── persistence/       # jOOQ リポジトリ実装
│   │   ├── cache/             # Redis キャッシュリポジトリ実装
│   │   └── mail/              # SES メール送信
│   ├── http/                  # プレゼンテーション層
│   │   ├── controllers/       # Spring MVC コントローラー
│   │   ├── requests/
│   │   └── responses/
│   └── support/               # 例外・エンティティ基底クラス等の共通基盤
├── src/test/java/             # Serviceのユニットテスト（Fakeリポジトリ）
├── src/main/resources/db/schema.sql   # jOOQ コード生成用スキーマ
├── build.gradle.kts
└── settings.gradle.kts
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `spring-boot-starter-web` | HTTP フレームワーク |
| `jooq` | ORM（タイプセーフ SQL ビルダー・コード生成） |
| `mysql-connector-j` / `HikariCP` | MySQL ドライバー・コネクションプール |
| `jedis` | Redis クライアント |
| `nimbus-jose-jwt` | JWT 生成・検証（RS256） |
| `mapstruct` | Entity/Record 間のオブジェクトマッピング |
| `software.amazon.awssdk:ses` | AWS SES メール送信 |

---

## :rocket: セットアップ

### 1. 依存パッケージのビルド

```bash
gradle build
```

### 2. 環境変数の設定

```bash
cp .env.example .env
```

以下を設定してください。

```dotenv
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
```

### 3. 起動

```bash
gradle bootRun
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :test_tube: テスト

```bash
gradle test
```

JUnit 5 によるServiceのユニットテストです。手書きの Fake リポジトリを使い、モックライブラリには依存していません。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-java.sh up    # 起動
bin/docker-java.sh down  # 停止
bin/docker-java.sh exec  # コンテナに入る
```
