<p align="center">
<a href="https://learn.microsoft.com/dotnet/csharp/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/csharp/csharp-original.svg" height="72" alt="C#"></a>
&nbsp;&nbsp;
<a href="https://learn.microsoft.com/aspnet/core/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/dotnetcore/dotnetcore-original.svg" height="72" alt="ASP.NET Core"></a>
</p>

<p align="center">
<a href="https://dotnet.microsoft.com/download"><img src="https://img.shields.io/badge/.NET-10-512BD4?logo=dotnet&logoColor=white" alt=".NET 10"></a>
<a href="https://learn.microsoft.com/aspnet/core/"><img src="https://img.shields.io/badge/ASP.NET_Core-Minimal_APIs-512BD4?logo=dotnet&logoColor=white" alt="ASP.NET Core Minimal APIs"></a>
<a href="https://learn.microsoft.com/ef/core/"><img src="https://img.shields.io/badge/EF_Core-9.0-512BD4?logo=dotnet&logoColor=white" alt="EF Core 9.0"></a>
</p>

---

## :book: 概要

認可サーバー API の **C# / ASP.NET Core** バックエンド実装です。

Google / GitHub OAuth によるスタッフ認証・JWT 発行と検証・クライアント管理・通知管理を担います。
PHP / Laravel 実装と同一 MySQL スキーマを共有し、完全な機能互換を持ちます。
API 仕様は [`docs/api-spec/openapi.yml`](../../docs/api-spec/openapi.yml) を参照してください。

ASP.NET Core の Minimal API とプライマリコンストラクタ、`record` によるイミュータブルなドメイン
エンティティを積極的に採用しています。

---

## :building_construction: アーキテクチャ

DDD + クリーンアーキテクチャを採用しています。

```
HTTP Request
    │
    ▼
Handler (src/Authorization.Api/Handler/)
    │  リクエスト解析・レスポンス整形（Minimal API のルーティング先）
    ▼
UseCase / Service (src/Authorization.Api/UseCase/)
    │  ビジネスロジック・鍵ペア生成・JWT 操作
    │  Domain Repository インターフェースに依存（依存性逆転）
    ▼
Domain (src/Authorization.Api/Domain/)
    │  エンティティ（record）・リポジトリインターフェース・値オブジェクト
    ▼
Infrastructure (src/Authorization.Api/Infrastructure/)
    │  EF Core リポジトリ実装・Redis キャッシュ
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
backends/csharp-aspnet/
├── src/Authorization.Api/
│   ├── Program.cs                # エントリーポイント・DI 組み立て・ルーティング登録
│   ├── AppModule.cs              # DI登録・Minimal API ルート定義
│   ├── Config/                   # 環境変数読み込み
│   ├── Domain/                   # ドメイン層
│   │   ├── Client/                # エンティティ（record）・リポジトリ IF・値オブジェクト
│   │   ├── Staff/
│   │   ├── Invitation/
│   │   ├── Notification/
│   │   └── Gate/
│   ├── UseCase/                  # ユースケース層
│   │   ├── Client/                # DTO・Service
│   │   ├── Staff/
│   │   ├── Auth/
│   │   ├── Invitation/
│   │   ├── Notification/
│   │   └── Gate/
│   ├── Infrastructure/           # インフラ層
│   │   ├── Model/                 # EF Core エンティティ（DB スキーマ定義）
│   │   ├── Persistence/           # EF Core リポジトリ実装
│   │   ├── Cache/                 # Redis キャッシュリポジトリ実装
│   │   ├── Mail/                  # SES メール送信
│   │   └── Db/                    # DbContext
│   ├── Handler/                  # Minimal API ハンドラー層
│   ├── Http/Responses/           # レスポンス整形クラス
│   └── Support/                  # 例外・ページング等の共通基盤
└── tests/Authorization.Api.Tests/
    └── UseCase/                  # Serviceのユニットテスト（Fakeリポジトリ）
```

---

## :package: 主要パッケージ

| パッケージ | 用途 |
|---|---|
| `Microsoft.EntityFrameworkCore` / `Pomelo.EntityFrameworkCore.MySql` | ORM（MySQL） |
| `StackExchange.Redis` | Redis クライアント |
| `System.IdentityModel.Tokens.Jwt` | JWT 生成・検証（RS256） |
| `Mapster` | Entity/DTO/ValueObject 間のオブジェクトマッピング |
| `AWSSDK.SimpleEmail` | AWS SES メール送信 |

---

## :rocket: セットアップ

### 1. 依存パッケージの取得

ソリューションファイルは無いため、プロジェクトごとに指定します。

```bash
dotnet restore src/Authorization.Api
dotnet restore tests/Authorization.Api.Tests
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
dotnet run --project src/Authorization.Api
```

Docker 環境では `docker compose up -d` で自動起動します。

---

## :test_tube: テスト

```bash
dotnet test tests/Authorization.Api.Tests
```

Service 層のユニットテスト（xUnit）です。手書きの Fake リポジトリ（一部は EF Core の SQLite
インメモリプロバイダ）を使い、モックライブラリや実 DB/Redis 接続には依存していません。
ローカル・CI とも同じコマンドで実行できます。

---

## :whale: Docker

```bash
# docker/ ディレクトリから実行
bin/docker-csharp.sh up    # 起動
bin/docker-csharp.sh down  # 停止
bin/docker-csharp.sh exec  # コンテナに入る
```
