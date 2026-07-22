# コーディング規約（AI コーディングエージェント向け）

このファイルは GitHub Copilot などの AI コーディングエージェントがこのリポジトリで
提案・生成を行う際に参照する規約です。人間のコントリビューターにとっても
プロジェクトの慣習をまとめたリファレンスとして使えます。

## :book: プロジェクト概要

このリポジトリは OAuth2/OIDC 準拠の認可サーバーです。**マイクロサービス分割ではなく**、
同一の OpenAPI 仕様（[`docs/api-spec/openapi.yml`](../docs/api-spec/openapi.yml)）に沿った
バックエンド実装を、言語・フレームワークごとに `backends/` 配下へ並べています
（Go×3 フレームワーク、PHP/Laravel、Python/FastAPI、TypeScript/Hono、Rust/Axum、
Kotlin/Ktor、Ruby×2 フレームワーク）。実験・プロトタイプ用ではなく、
どの実装も同じ仕様を満たす本実装として扱ってください。

- `frontend/` — 認可管理画面（Next.js）。規約は [`frontend/AGENTS.md`](../frontend/AGENTS.md) /
  [`frontend/CLAUDE.md`](../frontend/CLAUDE.md) も参照（Next.js の破壊的変更に関する注意書きがある）
- `function/` — AWS Lambda（Go）。ローカルでも本番と同じ Lambda 経由の経路で動作確認すること
  （API Gateway エミュレーター／LocalStack をバイパスしない）
- `backends/*` — 各言語バックエンド実装。共通の OpenAPI 仕様に準拠する
- `terraform/` — 環境別 IaC（`local` / `develop` / `staging` / `production`）
- `docker/` — `BACKEND_MODE` でバックエンド実装を切り替えるコンテナ定義

## :twisted_rightwards_arrows: ブランチ運用

- `develop` から派生した Issue 単位のブランチで作業する
- ブランチ名: `feature/issue-<Issue番号>`（バグ修正主体の場合は `fix/issue-<Issue番号>` の例もある）
- 作業完了後は `develop` への PR を作成する（`main` への直接 PR は作らない）
- 明示的な指示がない限り、ブランチ作成・コミット・push・PR 作成は勝手に行わない

## :memo: Issue 運用

- タイトル形式: `[auth] <type>: <日本語の概要>`
  （`type` は `feat` / `fix` / `docs` / `refactor` / `design` など）
- 複数 Issue にまたがる修正では `fix(#86/#87/#88): ...` のように対象 Issue 番号を併記してよい
- 本文は概要・実装内容・完了条件（チェックリスト）の構成が基本
- Issue をクローズする前に、本文のチェックリスト（`- [ ]`）を実態に合わせて `- [x]` に更新する
- 作業中に見つけた本来のスコープ外の実バグなど、特記事項があればクローズ時にコメントとして残す

## :pencil2: コミットメッセージ規約

- 形式: `<type>(#<Issue番号>): <日本語で概要>`
  例: `fix(#134): QR ページで SVG 描画を待機してから humanDelay`
- `type` は Issue のタイプ規約と同じ（`feat` / `fix` / `docs` / `refactor` など）
- 1 コミット 1 目的を意識し、性質の異なる変更（機能修正・テスト修正・ドキュメント更新など）は分けてコミットする
- push 後は該当 PR に修正内容のコメントを残す（まとめて後から報告しない）

## :globe_with_meridians: 言語横断の規約

- **クラス参照は `use` 文でインポートする**。フルパス直書きは避ける
  （PHPDoc の型表記や IDE 互換など、明確な理由がある場合のみ例外）
- **新規バックエンドを追加する場合は必ずロケール／タイムゾーンを設定する**
  （例: PHP/Laravel では `.env` に `APP_TIMEZONE=Asia/Tokyo` / `APP_LOCALE=ja`）。
  未設定だとコンテナが UTC のままになり時刻が 9 時間ずれる
- `.env` 構成は `.env` / `.env.example` / `.env.testing` / `.env.testing.local` の 4 ファイル構成が基準
  （`.env.local` のような独自ファイルは追加しない）
- PHPDoc の `@throws` は具体的な例外クラスを明記する。情報量のない `\Throwable` への書き換えは行わない

## :elephant: PHP (Laravel) 固有の規約

- 全ファイル先頭に以下のヘッダーコメントを付ける（`declare(strict_types=1)` も必須）

  ```php
  <?php

  /**
   * This is a program developed by BobTabo.
   *
   * Copyright (c) 2026 BobTabo. All Rights Reserved.
   */

  declare(strict_types=1);
  ```

- エラーハンドリングは `App\Support\Exceptions\AppException` を `throw` し、
  `bootstrap/app.php` の `withExceptions` で一元的にレスポンス変換する。
  コントローラー内で直接 `response()->failure()` を書かない
- `app/Support/` 配下は複数バックエンド共通の基盤であり、過去に変更で他機能を壊した経緯がある。
  **変更前に必ず一声かけてから着手する**

### DDD + クリーンアーキテクチャ（php-laravel）

`app/` は DDD（ドメイン駆動設計）+ クリーンアーキテクチャの層構成になっている。
新規実装・修正はこの層構成と依存方向（`Http → UseCases → Domain ← Infrastructure`）を崩さないこと。

| 層 | ディレクトリ | 役割 |
|---|---|---|
| Presentation | `app/Http/Controllers`, `Requests`, `Responses` | リクエスト受付・レスポンス整形のみ。ビジネスロジックを持たない |
| Application | `app/UseCases/<Domain>/**Service.php`, `Dtos` | ユースケース単位の処理。Controller から呼ばれる |
| Domain | `app/Domain/<Domain>/{Entities,ValueObjects,Repositories,Condition,Enums}` | ビジネスルールの中心。`Repositories` はインターフェースのみを置く（実装を持たない） |
| Infrastructure | `app/Infrastructure/Persistence/Eloquent**Repository.php`, `app/Infrastructure/Models` | `Domain/**/Repositories` の実装。Eloquent への依存はここに閉じ込める |
| Support | `app/Support/**` | 上記どの層からも参照されうる共通基盤（Exceptions/Traits/ValueObjects 等） |

レイヤー間インターフェースのルール:

- **Service (UseCase)**: 引数は **Dto** に統一、戻り値は **ValueObject** または **void**。
  Repository インスタンス名は `$repository`（複数ある場合は Repository 名のキャメルケース）
- **Repository（インターフェース）**: 引数は **Entity** または **Condition**（検索系のみ）。
  戻り値は **Entity** / **Collection\<Entity\>** / **void** / **bool**（Delete 系のみ） / **int**（件数のみ）
- **Controller**: Service インスタンス名は `$service`（複数ある場合は Service 名のキャメルケース）。
  登録・更新・削除は 1 件でも必ずトランザクション処理し、トランザクションの外で実行しない
- 配列・コレクションの変数名は複数形、または `list` / `○○List` を使う

### クラス継承（php-laravel）

新規クラスを作る際は、必ず種別に応じた基底クラス/インターフェースを継承・実装する。
毎回指示せずとも、以下のスケルトンに従うこと（`namespace` は配置ディレクトリに合わせて読み替える）。

**Controller** — `app/Http/Controllers/Api/`

```php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;

class ExampleController extends Controller
{
}
```

**Form Request** — `app/Http/Requests/<Domain>/`

```php
namespace App\Http\Requests\Example;

use App\Support\Http\Requests\AppRequest;

class StoreExampleRequest extends AppRequest
{
}
```

**Response（単票）** — `app/Http/Responses/<Domain>/`

```php
namespace App\Http\Responses\Example;

use App\Support\Http\Responses\AbstractResponse;

class ExampleResponse extends AbstractResponse
{
}
```

**Response（一覧・ページング）** — `app/Http/Responses/<Domain>/`

```php
namespace App\Http\Responses\Example;

use App\Support\Http\Responses\PagerResponse;

class ExampleIndexResponse extends PagerResponse
{
}
```

**Service（UseCase）** — `app/UseCases/<Domain>/`

```php
namespace App\UseCases\Example;

use App\Support\Services\AbstractService;

class ExampleService extends AbstractService
{
}
```

**Repository（インターフェース）** — `app/Domain/<Domain>/Repositories/`

```php
namespace App\Domain\Example\Repositories;

interface ExampleRepository
{
}
```

**Repository（実装）** — `app/Infrastructure/Persistence/`

```php
namespace App\Infrastructure\Persistence;

use App\Domain\Example\Repositories\ExampleRepository;
use App\Support\Repositories\AbstractEloquentRepository;

class EloquentExampleRepository extends AbstractEloquentRepository implements ExampleRepository
{
}
```

**Entity** — `app/Domain/<Domain>/Entities/`

```php
namespace App\Domain\Example\Entities;

use App\Support\Entities\AbstractEntity;

class Example extends AbstractEntity
{
}
```

**ValueObject** — `app/Domain/<Domain>/ValueObjects/`

```php
namespace App\Domain\Example\ValueObjects;

use App\Support\ValueObjects\AbstractValueObject;

class ExampleVo extends AbstractValueObject
{
}
```

**Dto（単体）** — `app/UseCases/<Domain>/Dtos/`

```php
namespace App\UseCases\Example\Dtos;

use App\Support\Dtos\AbstractDto;

class ExampleDto extends AbstractDto
{
}
```

**Dto（一覧・ページング）** — `app/UseCases/<Domain>/Dtos/`

```php
namespace App\UseCases\Example\Dtos;

use App\Support\Dtos\PagerDto;

class ExampleListDto extends PagerDto
{
}
```

**Condition（検索条件）** — `app/Domain/<Domain>/Condition/`

```php
namespace App\Domain\Example\Condition;

use App\Support\Repositories\Conditions\AbstractCondition;

class ExampleCondition extends AbstractCondition
{
}
```

**Eloquent Model（マスタデータ）** — `app/Infrastructure/Models/`

```php
namespace App\Infrastructure\Models;

use App\Support\Models\AppMasterModel;

class Example extends AppMasterModel
{
}
```

**Eloquent Model（トランザクションデータ）** — `app/Infrastructure/Models/`

```php
namespace App\Infrastructure\Models;

use App\Support\Models\AppTransactionModel;

class Example extends AppTransactionModel
{
}
```

補足:

- Repository の実装クラスは、対応する `Domain/**/Repositories` のインターフェースを必ず `implements` する
  （`AbstractEloquentRepository` を継承するだけでは不十分）
- 上記のどれにも当てはまらない共通処理を書く場合は、新しい基底クラスを `app/Support/` 配下に作ってから継承する
  （個別クラスに直接書かない）

## :test_tube: テスト

- 各バックエンドの起動・テスト手順は `backends/<dir>/README.md` を参照する
  （言語ごとに実行コマンドが異なるため、リポジトリ全体で共通のコマンドはない）
- 新規テストは実コードをそのまま書く。既存の一部テストに見られる「コメントアウトで無効化する」パターンは踏襲しない
- フロントエンドの `frontend/e2e/demo/` はデモ GIF 録画専用のシナリオで、CI では実行しない
  （`frontend/e2e/` の通常の E2E テストとは分離されている）

### テスト記述方法（backends/php-laravel/tests）

- Feature テストは `Tests\TestCase`（`tests/TestCase.php`）を継承し、`DatabaseMigrations` トレイトを使う

  ```php
  namespace Tests\Feature;

  use Illuminate\Foundation\Testing\DatabaseMigrations;
  use Tests\TestCase;

  class ExampleControllerTest extends TestCase
  {
      use DatabaseMigrations;

      public function testIndex(): void
      {
          $params = $this->getRequestParams('Example/index.json');
          $response = $this->get('/api/examples', $params);
          $data = $this->getResponseData('Example/index.json');
          $response
              ->assertStatus(200)
              ->assertJson($data);
      }
  }
  ```

- リクエスト/レスポンスは PHP に直書きせず、JSON フィクスチャに分離する
  - リクエスト: `tests/Feature/Requests/<Domain>/<method>.json` → `$this->getRequestParams('<Domain>/<method>.json')`
  - レスポンス: `tests/Feature/Responses/<Domain>/<method>.json` → `$this->getResponseData('<Domain>/<method>.json')`
  - 両メソッドとも第2引数 `$mergeData`（連想配列）でフィクスチャの値を個別に上書きできる
- テストメソッド名は `testXxx`（対象 Controller のメソッド名に対応させる）。1 Controller = 1 テストクラス
- スタッフ認証状態を再現する場合は `withStaffCookie(int $staffId)` を使う（`staff_id` は暗号化されないクッキーのため `withUnencryptedCookies` 経由で設定される）
- private/protected メソッドを直接検証したい場合は `reflectionMethod` / `reflectionProperty` / `executeMethod` を使う（新しく public にしない）

## :robot: AI コーディングエージェントへの振る舞い方針

- **明示的な指示があるまで、コミット・push・PR 作成・Issue クローズを行わない**
- コードやテストの実行結果を元に不具合を見つけても、指摘元のコードを**勝手にリバートしない**。
  ユーザーの判断を待つ
- Docker コンテナを停止する必要がある場合は、停止前に一言伝え、
  `bin/docker-xxx-down.sh` 相当のスクリプトで実行してもらう（`docker stop` を直接叩かない）
- ローカル環境での動作確認は本番同様に Lambda（Port: 9000）経由で行う。直接バックエンドを叩いてバイパスしない
- 破壊的操作（`git reset --hard` / force push / ファイル削除など）は、明示的な許可なく実行しない

## :hook: Claude Code Hooks（`.claude/settings.json`）

このリポジトリは Claude Code の Hooks 設定 `.claude/settings.json` を Git 管理し、チーム全体で
共有している（`.gitignore` は `.claude/*` を無視しつつ `!.claude/settings.json` だけ追跡対象にしている。
`.claude/settings.local.json` と `.claude/worktrees/` は引き続き無視される）。設定済みの Hooks は以下のとおり。
Hooks の仕組みは https://docs.claude.com/en/docs/claude-code/hooks を参照。

- **フォーマッタ/Lint はすべてホスト側で直接実行する**（`docker exec` は使わない）。Hooks からは
  `$CLAUDE_PROJECT_DIR` からの相対パスで各コマンドを直接叩く。各ツールがホストに無い場合は
  `command -v` ガードで自動的にスキップされる（下記「必要なツール」を参照）。

### PostToolUse（Edit / Write / MultiEdit 直後）

1. **コードフォーマット + import 整理の自動化**。編集ファイルのパスで対象バックエンドを判定して実行する。
   - `backends/php-laravel/*.php` → `vendor/bin/pint`（1 コマンドで整形と import 整理を実行）
   - `backends/go-*/*.go`・`function/*.go` → `goimports -w`（gofmt 相当の整形 + 未使用 import 削除・並び替えを 1 コマンドで実行。別途 `gofmt` は不要）
   - `backends/python-fastapi/*.py` → `ruff format` → `ruff check --fix --select I,F401` → `ruff format`（**フォーマット → import 整理 → 再フォーマットの順。逆順にしない**）
   - `backends/rust-axum/*.rs` → `cargo fmt` → `cargo fix --allow-dirty --allow-staged` → `cargo fmt`（同上の順序。クレート全体を対象に実行する）
     - 注意: `cargo fix` はコンパイラが提案する修正を無制限に適用するため、他言語（未使用 import・並び替えに限定）より自動変更の範囲が広い。編集のたびに意図しない変更が入る可能性があるので、フォーマット後は差分を確認すること。
   - `backends/kotlin-ktor/*.{kt,kts}` → `gradle ktlintFormat`（整形と import 順序を 1 コマンドで処理。モジュール全体が対象。`build.gradle.kts` で `ktlintCheck` 系は `check`/`build` から外してあるため CI の `gradle build` は汚さない）
   - `backends/ruby-{rails,hanami}/*.rb` → `bundle exec rubocop -a`（Ruby に import 最適化の概念は無いため整形のみ）
   - `frontend/*.{ts,tsx}` → `eslint --fix`（**Prettier は導入しない**。`unused-imports` プラグインで未使用 import 削除、`import/order` で並び替えを行う。`eslint-import-resolver-typescript` により `tsconfig` の `@/*` パスエイリアスを internal として解決する）
   - 注: `backends/ts-hono` はフォーマッタ未設定のため対象外。
2. **静的解析（Lint）チェック**。フォーマット後に警告があれば **stderr に出して通知するだけ（ブロックしない）**。
   - 対象は軽量で 1 ファイル単位に絞れるもののみ: `ruff check`（Python）/ `rubocop`（Ruby）/ `eslint`（frontend）。
   - **設計判断（実行時間について）**: Stop フックで毎回フルプロジェクト解析を行うと重くなるため採用していない。
     また `golangci-lint` / `clippy` / `phpstan` / `detekt` などコンパイルを伴う重いツールは、1 ファイル編集ごとに
     実行すると待ち時間が大きいため per-edit フックには載せていない。これらの本格的な静的解析は CI もしくは
     手動実行に委ねる方針。将来 per-edit で有効化する場合は変更ファイル/パッケージのみに絞ること。

### PreToolUse（実行前チェック）

- **危険な git 操作のガード**（matcher: `Bash`）: `git push --force` / `reset --hard` / `git branch -D` / `git clean -f` を
  検出したら確認を求める（`permissionDecision: ask`）。
- **Docker 停止コマンドのガード**（matcher: `Bash`）: `docker compose down/stop` や `docker stop` を直接叩こうとしたら
  `docker/bin/docker-<backend>.sh down` の利用を促して確認を求める（`ask`）。
- **`.env` 系ファイルへの直接編集をブロック**（matcher: `Edit|Write|MultiEdit`）: `.env` / `.env.local` / `.env.*.local` /
  `*.env` への編集を拒否する（`deny`）。テンプレートである `.env.example` / `.env.testing` は編集可能。
- **コミット前の機密情報混入チェック**（matcher: `Bash`）: `git commit` 実行前に差分をスキャンし、
  `api[_-]?key` / `secret` / `password` / `token` の代入や秘密鍵ヘッダ・AWS アクセスキー ID らしき文字列を検出したら
  拒否する（`deny`）。通常はステージ済み差分（`git diff --cached`）を見るが、`git commit -a`/`--all` の場合は追跡済み
  ファイルの未ステージ変更もコミット対象になるため `git diff HEAD` を対象にする。将来的に `gitleaks` 等の専用ツール導入も検討。

### Stop（応答完了時）

- **タスク完了通知**: `notify-send`（Linux）または `osascript`（macOS）でデスクトップ通知を出す。どちらも無い環境では何もしない。

### Hooks が前提とするツール / 依存関係

- 依存ファイルに追加済み: `ruff`（`backends/python-fastapi/requirements.txt`）、`rubocop`（`backends/ruby-rails/Gemfile` /
  `backends/ruby-hanami/Gemfile` の `:development` グループ）、ktlint Gradle プラグイン（`backends/kotlin-ktor/build.gradle.kts`）、
  `eslint-plugin-unused-imports` / `eslint-plugin-import` / `eslint-import-resolver-typescript`（`frontend/package.json`）。
- ホスト側に別途必要: `goimports`（`go install golang.org/x/tools/cmd/goimports@latest`。`$(go env GOPATH)/bin` を
  PATH に通しておくこと）、`jq`、および `pint` 用に php-laravel の `composer install`（`vendor/bin/pint` が生成される）。
- `cargo` / `rustfmt`（rustup）と `gradle` はホストに導入済みである前提（README のビルド手順どおり）。
