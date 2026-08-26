---
name: backend-add-usecase
description: >-
  backends/ 配下のいずれかのバックエンドに新しいユースケース（APIエンドポイントや
  ドメイン処理）を追加・変更する際に使う。DDD + クリーンアーキテクチャの層構成、
  Dto/Entity/ValueObject/Condition/Repository の規約、トランザクションの扱い、
  テストの流し方を定める。どのバックエンドを触るかの判断は backend-dispatch Skillを参照。
allowed-tools: Bash(docker:*), Bash(git:*), Bash(rg:*)
---

# backend-add-usecase

## 0. 前提

- 対象バックエンドが確定していること。言語名だけ指示された場合（例「Goで」）は
  backend-dispatch Skillで確定させる。GoはGin/Echo/Beego、RubyはRails/Hanamiの3/2択。
- APIの正は `docs/api-spec/openapi.yml`。エンドポイントの追加・変更はここを必ず更新する。
- 10バックエンドは同一仕様を実装する。**1バックエンドだけ挙動を変える変更は原則しない**。
  1本だけ直す指示なら、他9本にも同じ修正が必要かをユーザーに確認する。
- ソースの編集はホスト側で行い、ビルド・テスト・CLIはコンテナ内で実行する（docker-ops Skill）。

## 1. スコープの決定: 共通スケルトン1本 + 言語別の対応表

**このSkillは言語別に分割せず、共通1本とする。** 10バックエンドは同一のOpenAPI仕様を
実装しており、層構成・依存方向・レイヤー間I/F（引数Dto／戻り値VO／Repositoryは
Entity・Condition）は全言語で同一だからである。言語ごとに違うのは
「ディレクトリ名・ファイル名・テスト実行コマンド」だけなので、それを下記の対応表で
引けるようにしている。

言語固有のスケルトン（継承すべき基底クラス等）は、php-laravel のみ
`.github/copilot-instructions.md`「クラス継承（php-laravel）」に一覧がある。
他言語は同ドメインの既存ファイルを読んで合わせる（本Skillに言語別スケルトンを
重複記載すると、実装が変わったときに乖離するため書かない）。

## 2. 層構成は全バックエンド共通、ディレクトリ名だけが言語別

依存方向は全バックエンド共通で **Presentation → Application(UseCase) → Domain ← Infrastructure**。
`Domain` にはリポジトリの**インターフェースのみ**を置き、実装は `Infrastructure` に閉じる。
DB/ORM への依存が `Domain` や `UseCase` に漏れていたらその時点で設計ミス。

| バックエンド | ルート | Presentation | Application | Domain | Infrastructure |
|---|---|---|---|---|---|
| `go-gin` / `go-echo` / `go-beego` | `internal/` | `handler/` | `usecase/<domain>/{dto.go,interactor.go}` | `domain/<domain>/{entity,repository,value_objects}.go` | `infrastructure/{persistence,model,db,cache,mail}/` |
| `kotlin-ktor` | `src/main/kotlin/com/authorization/` | `handler/` | `usecase/` | `domain/` | `infrastructure/` |
| `php-laravel` | `app/` | `Http/{Controllers/Api,Requests,Responses}` | `UseCases/<Domain>/{<X>Service.php,Dtos/}` | `Domain/<Domain>/{Entities,ValueObjects,Repositories,Condition,Enums}` | `Infrastructure/{Persistence,Models,Cache}` |
| `python-fastapi` | `app/` | `routers/` | `usecase/` | `domain/<domain>/{entity,repository,value_objects}.py` | `infrastructure/` |
| `ruby-rails` | `app/` | `controllers/`, `requests/` | `usecase/` | `domain/<domain>/{entity,repository,value_objects}.rb` | `infrastructure/` |
| `ruby-hanami` | `app/` | `actions/`, `requests/` | `usecase/` | `domain/` | `infrastructure/` |
| `rust-axum` | `src/` | `handler/` | `usecase/` | `domain/<domain>/` | `infrastructure/` |
| `ts-hono` | `src/` | `routes/` | `usecase/` | `domain/<domain>/{entity.ts,repository.ts,valueObjects.ts}` | `infrastructure/` |

どのバックエンドにも `support/`（php-laravel は `app/Support/`）と `config/`、`middleware/` がある。
共通処理を個別クラスに直接書かず、`support/` に基底クラス・ヘルパを作ってから使う。

**言語別スケルトンの調べ方（新規ファイルを書く前に必ず実施）**: 同じドメイン層の
既存ファイルを読み、命名・シグネチャ・エラーハンドリングをそのまま踏襲する。

```bash
set -euo pipefail
BACKEND=go-gin        # 対象バックエンドに置き換える
ls -R "backends/${BACKEND}" | head -60
# 参考にする既存ユースケース（notification は比較的小さく読みやすい）
rg -l notification "backends/${BACKEND}" --glob '!vendor' --glob '!node_modules' | head -20
```

php-laravel は各クラスが継承すべき基底クラスが `.github/copilot-instructions.md`
「クラス継承（php-laravel）」にスケルトンとして列挙されている。**必ずそれに従う**
（`AbstractService` / `AbstractEntity` / `AbstractValueObject` / `AbstractDto` / `PagerDto` /
`AbstractCondition` / `AbstractEloquentRepository` / `AppMasterModel` / `AppTransactionModel` 等）。
他言語には同等の一覧がないため、既存の同種ファイルを読んで合わせる。

## 3. レイヤー間インターフェースの規約

- **UseCase（Service / Interactor）**: 引数は **Dto** に統一、戻り値は **ValueObject** または
  **void**。Repository の変数名は `repository`（複数ある場合は Repository 名のキャメルケース）。
- **Repository（インターフェース）**: 引数は **Entity**、検索系のみ **Condition**。戻り値は
  **Entity** / **Entityのコレクション** / **void** / **bool**（削除系のみ） / **int**（件数のみ）。
- **Presentation（Controller / Handler / Router / Action）**: Service の変数名は `service`
  （複数ある場合は Service 名のキャメルケース）。リクエストの検証はここで行い、
  ビジネスロジックを持たない。
- 配列・コレクションの変数名は複数形、または `list` / `○○List`。
- 一覧・ページングは単票用と別のDto/Response（php-laravel なら `PagerDto` / `PagerResponse`）を使う。

### トランザクション

- **登録・更新・削除は1件でも必ずトランザクション内で実行する**。
- トランザクションの境界は Presentation 層（Controller / Handler）に置き、
  トランザクションの外でDB更新を実行しない。
- 楽観ロック（`version` カラム等）を持つテーブルは、更新時に必ずバージョンを条件に含める。
  既存の同種ユースケースの実装をそのまま踏襲する（独自方式を発明しない）。

## 4. 追加手順

1. `docs/api-spec/openapi.yml` でエンドポイント・リクエスト/レスポンスを確定する
   （既存エンドポイントの変更なら現在の定義を読む）。
2. Domain: Entity / ValueObject / （検索系なら）Condition / Repository インターフェースを追加。
3. Infrastructure: Repository 実装（+ 必要ならモデル）を追加。ORM 依存はここに閉じる。
4. Application: Dto と UseCase（Service / Interactor）を追加。引数Dto・戻り値VOを守る。
5. Presentation: Controller / Handler / Router を追加し、ルーティングとDI登録を行う
   （DI/ルート定義の場所は言語ごとに違うので既存エンドポイントを `rg` して探す）。
6. マイグレーションが必要なら、そのバックエンドの既存マイグレーション方式に従う
   （生成ファイルは手編集せず生成コマンドを使う）。
7. テストを追加する（次項）。既存テストの「コメントアウトで無効化」パターンは踏襲しない。
8. `docs/architecture/backend.puml` に影響する構成変更なら合わせて更新する。

## 5. 動作確認（少なくとも1バックエンドで必ず実施）

コンテナに入ってテストを流す。起動は docker-ops Skill（`docker-common.sh up` →
個別スクリプト `up`）。`exec` でシェルに入る:

```bash
set -euo pipefail
cd docker
bin/docker-php.sh exec      # 対象バックエンドのスクリプトに置き換える
```

コンテナ内で実行するテストコマンド（CIと同じもの）:

| バックエンド | テスト |
|---|---|
| `go-gin` / `go-echo` / `go-beego` | `go test ./tests/...` |
| `kotlin-ktor` | `gradle test` |
| `php-laravel` | `php artisan migrate` → `./vendor/bin/phpunit` |
| `python-fastapi` | `python -m pytest tests/ -v` |
| `ruby-rails` / `ruby-hanami` | `bundle exec rspec` |
| `rust-axum` | `cargo test --lib` と `cargo test --test integration -- --test-threads=1` |
| `ts-hono` | `npm test`（+ `npm run build`） |

- テストはテスト用DB（`authorization_test`）を使う。スキーマは `backends/go-gin/tests/schema.sql`
  が事実上の共通スキーマで、ruby系CIはこれを流し込んでいる。テーブル追加時はここも更新が必要か確認する。
- API経由の確認は Lambda / API Gateway エミュレーション経由で行う（直接ポートを叩かない）。
  経路の詳細は backend-dispatch / docker-ops Skill を参照。
- `feature/issue-*` ブランチではCIが自動発火しない。CIで確認したい場合は
  backend-ci-trigger Skill で対象ワークフローを手動発火する。

## 6. 落とし穴

- **タイムゾーン/ロケール**: バックエンドは `Asia/Tokyo` / `ja` 前提。新規実装で日時を扱う際に
  UTC のまま返すと9時間ずれる。既存実装の日時整形をそのまま使う。
- **`.env` 4ファイル構成**（`.env` / `.env.example` / `.env.testing` / `.env.testing.local`）を
  崩さない。新しい環境変数を足すときは `.env.example` と `.env.testing` にも追記する
  （`.env` の実値はコミットしない）。
- Repository 実装がインターフェースを `implements` していない（基底クラス継承だけ）は
  よくあるミス。php-laravel では特に注意。
- 10バックエンド共通仕様の変更（OpenAPI変更、共通スキーマ変更）は、1本だけ直して
  終わらせない。範囲が曖昧ならユーザーに確認する。
