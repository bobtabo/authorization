---
name: backend-dispatch
description: >-
  「Goのバックエンドで◯◯を直して」のように言語名だけで指示された際に、正しい
  backends/ 配下のディレクトリと docker/bin/docker-*.sh のラッパースクリプトへ
  振り分ける際に使う。Go（gin/echo/beego）やRuby（rails/hanami）のように
  同一言語で複数フレームワークがある指示の解決にも使う。
allowed-tools: Bash(docker compose:*), Bash(docker/bin/docker-*.sh:*), Bash(cd:*)
---

# backend-dispatch

このリポジトリは同一の OpenAPI 仕様（`docs/api-spec/openapi.yml`）に沿った認可サーバーAPIを
10言語・フレームワークで実装している（マイクロサービス分割ではない）。
どのバックエンドを指しているかを機械的に確定させ、実行は必ず
`docker/bin/docker-*.sh` 経由のコンテナ内で行う。

## 依存Skill（マージ順）

本Skillは以下を参照する。いずれも #162〜#169 で同時期に追加されるため、
それらが `develop` にマージされる前は参照先が存在しない場合がある（段階的導入）。

| 参照先Skill | 追加Issue / PR | 参照している内容 | 未マージ時の代替 |
|---|---|---|---|
| docker-ops | #168 / PR #176 | 起動・停止・一括Docker操作 | `docker/bin/docker-*.sh` を `-h` なしで読む |
| frontend-add-feature-page | #164 / PR #172 | フロントエンドの機能追加 | `frontend/AGENTS.md` |
| backend-ci-trigger | #169 / PR #177 | CIの手動発火とログ確認 | `.github/workflows/*.yml` を直接読む |

参照を全て解消した状態で入れたい場合は、#176 / #172 / #177 を本SkillのPR（#175）より
先にマージする。本Skill単体（対応表と曖昧な指示の解消）は参照先なしでも成立する。

## 対応表

| 呼ばれ方の例 | ソースディレクトリ | Docker操作スクリプト | composeディレクトリ | composeプロジェクト名 | execのサービス名 | シェル |
|---|---|---|---|---|---|---|
| Go, Gin | `backends/go-gin/` | `docker/bin/docker-go-gin.sh` | `docker/local/app-go-gin/` | `auth-go-gin` | `go` | `sh` |
| Go, Echo | `backends/go-echo/` | `docker/bin/docker-go-echo.sh` | `docker/local/app-go-echo/` | `auth-go-echo` | `go` | `sh` |
| Go, Beego | `backends/go-beego/` | `docker/bin/docker-go-beego.sh` | `docker/local/app-go-beego/` | `auth-go-beego` | `go` | `sh` |
| Kotlin, Ktor | `backends/kotlin-ktor/` | `docker/bin/docker-kotlin.sh` | `docker/local/app-kotlin/` | `auth-kotlin` | `kotlin` | `bash` |
| PHP, Laravel | `backends/php-laravel/` | `docker/bin/docker-php.sh` | `docker/local/app-php/` | `auth-php` | `php` | `bash` |
| Python, FastAPI | `backends/python-fastapi/` | `docker/bin/docker-python.sh` | `docker/local/app-python/` | `auth-python` | `python` | `bash` |
| Ruby, Hanami | `backends/ruby-hanami/` | `docker/bin/docker-rb-hanami.sh` | `docker/local/app-rb-hanami/` | `auth-rb-hanami` | `rb-hanami` | `bash` |
| Ruby, Rails | `backends/ruby-rails/` | `docker/bin/docker-rb-rails.sh` | `docker/local/app-rb-rails/` | `auth-rb-rails` | `rb-rails` | `bash` |
| Rust, Axum | `backends/rust-axum/` | `docker/bin/docker-rust.sh` | `docker/local/app-rust/` | `auth-rust` | `rust` | `bash` |
| TypeScript, Hono | `backends/ts-hono/` | `docker/bin/docker-ts.sh` | `docker/local/app-ts/` | `auth-ts` | `ts` | `sh` |

スクリプト名はディレクトリ名と一致しない（`ruby-rails` → `docker-rb-rails.sh`、
`kotlin-ktor` → `docker-kotlin.sh` 等）。composeディレクトリ（`docker/local/app-*`）も
composeプロジェクト名（`auth-*`）と接頭辞が違う。**いずれも名前から機械的に導出せず、
必ず上表を引く**。

## 曖昧な指示の解決

- 「Goで直して」→ `go-gin` / `go-echo` / `go-beego` のどれか特定できないため、
  推測で決めずユーザーに確認する。
- 「Rubyで直して」→ `ruby-rails` / `ruby-hanami` のどちらか確認する。
- フレームワーク名だけの指示（「Ginで」「Hanamiで」）は一意に決まるので確認不要。
- 「PHPで」「Pythonで」「Rustで」「Kotlinで」「TypeScriptで」は各1実装のみなので一意。
- 「全バックエンドで」「10言語すべてで」→ 上表の10ディレクトリすべてに同じ変更を展開する。
  一括Docker操作は `docker/bin/docker-backends.sh {up|down}`（docker-ops Skill参照）。
- マイグレーションは PHP（Laravel）に一本化されている。「マイグレーションを追加して」は
  フレームワーク指定が無くても `backends/php-laravel/` が対象（他バックエンドは
  テスト用スキーマ定義を個別に持つ）。
- フロントエンド（`frontend/`）・Lambda（`function/`）はこのSkillの対象外。
  フロントエンドの機能追加は frontend-add-feature-page Skillを参照。

## 実行方針

- **ソース編集**: `backends/<dir>/` はコンテナへbind mountされているため、ホスト側で
  そのまま編集してよい（Edit/Writeツールで通常どおり編集する）。
  編集後は `.claude/settings.json` のPostToolUseフックがホスト側でフォーマッタ／Lintを
  自動実行する（`backends/ts-hono` はフォーマッタ未設定のため対象外）。
- **テスト・ビルド・CLI実行**: 必ずコンテナ内で行う。ホストに言語ランタイムが入っていても
  ホストで直接 `go test` 等を実行しない。

  ```bash
  # 対応表の「Docker操作スクリプト」を使ってコンテナ内シェルに入る
  docker/bin/docker-go-gin.sh exec
  # 入った後、コンテナ内で通常のテスト/ビルドコマンドを実行する
  ```

- 非対話で1コマンドだけ実行したい場合は、compose ファイルとプロジェクト名を明示する
  （コンテナ名を直書きしない。プロジェクト名・サービス名がバックエンドごとに異なるため
  取り違えやすい）。コマンドは配列で書き `"${CMD[@]}"` で展開する（文字列のまま `$CMD`
  と書くと、引数中のクォートや `*` がホスト側シェルで単語分割・パス展開される）:

  ```bash
  set -euo pipefail
  PROJECT=auth-go-gin        # 対応表の「composeプロジェクト名」
  DIR=app-go-gin             # 対応表の「composeディレクトリ」（app-* であり auth-* ではない）
  SERVICE=go                 # 対応表の「execのサービス名」
  CMD=(go test ./...)        # 実行したいコマンド（配列で書く）

  # ラッパースクリプトと同じく compose ディレクトリで実行する
  # （compose ファイル内の相対パスマウントが実行位置に依存するため）
  cd "docker/local/${DIR}"
  docker compose -p "$PROJECT" -f docker-compose.yml \
    exec -T --user 1000 "$SERVICE" "${CMD[@]}"
  ```

  `-T` は必須（付けないとTTY割り当てを試みて非対話環境で失敗しうる）。
  `--user 1000` とサービス名は `docker/bin/docker-*.sh` の `exec` 分岐と揃える
  （スクリプト側は v1 の `docker-compose` を使っているが、手元では v2 の `docker compose` でもよい）。
- **APIの動作確認**は本番と同じ経路で行う。フロントエンドの Next.js は
  `/function/:path*` を `LAMBDA_PROXY_TARGET`（LocalStack API Gateway または
  エミュレータ）へ rewrite するので、dev サーバー経由なら
  `http://localhost:3000/function/<backend>/api/...` を叩く（`frontend/next.config.ts` 参照）。
  バックエンドコンテナを直接叩いてLocalStack・Lambdaをバイパスしない。
- コンテナが起動していない場合の対処は docker-ops Skillを参照。
- CIの発火は backend-ci-trigger Skillを参照（`feature/issue-*` ブランチでは自動実行されない）。

## 補足: 変更が複数バックエンドに波及するか

同一仕様の実装が10個あるため、仕様に関わる修正（OpenAPI・共通の振る舞い）は
1バックエンドだけ直すと実装間で挙動が食い違う。指示が1バックエンド限定か、
仕様変更として全バックエンドに展開すべきかが曖昧な場合はユーザーに確認する。
