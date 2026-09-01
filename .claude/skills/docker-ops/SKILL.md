---
name: docker-ops
description: >-
  docker/bin/docker-*.sh でコンテナを起動・停止・操作する際、またはコンテナが
  起動しない・APIが応答しない等のトラブル対応をする際に使う。
  「コンテナを起動して」「環境を立ち上げて」「コンテナに入って」等の指示で使う。
allowed-tools: Bash(bin/docker-*.sh:*), Bash(./bin/docker-*.sh:*), Bash(docker/bin/docker-*.sh:*),
  Bash(./docker/bin/docker-*.sh:*), Bash(docker compose:*), Bash(docker ps:*), Bash(cd:*),
  Bash(find:*), Bash(make:*)
---

# docker-ops

`docker/bin/*.sh` は共通インフラと各バックエンドのDocker操作を統一するラッパースクリプト。
コンテナの起動・停止・破棄（`up` / `down` / `start` / `stop`）は必ずこれらを経由する。
`.claude/settings.json` のPreToolUseフックが `docker compose down|stop` / `docker stop` の
直接実行を検出して確認を求めるため、直接叩かずラッパーを使う。

一方、起動済みコンテナへの `exec`（非対話の単発コマンド実行）はフックのブロック対象外で、
`docker compose -p <プロジェクト名> -f docker/local/<dir>/docker-compose.yml exec -T ...` を
直接使ってよい（backend-dispatch Skill参照）。

## 依存Skill

バックエンド名とディレクトリ・スクリプトの対応表は backend-dispatch Skill（Issue #167 /
PR #175）にある。同時期に追加されるSkillのため、`develop` にマージされる前は参照先が
存在しない場合がある。その場合は `docker/bin/` のスクリプト名と `docker/local/app-*` の
ディレクトリ名を直接見れば対応が分かる（`docker-<略称>.sh` ↔ `local/app-<略称>`）。

## 初回のみ

```bash
cd docker
find ./bin -type f -exec chmod 755 {} +
bin/docker-common.sh env        # 証明書と .env を配置（local/common/.env が作られる）
# local/common/.env の LOCALSTACK_AUTH_TOKEN を設定する（https://app.localstack.cloud/ で取得）
```

`.env` 系ファイルはフックで直接編集がブロックされるため、Edit/Writeツールではなく
ユーザーに手動設定を依頼する（`.env.example` / `.env.testing` は編集可）。

## 起動順序

`common` が Dockerネットワーク `authorization` と nginx-proxy を作る。**必ず `common` を先に
起動する**。各バックエンドの compose はこのネットワークを `external: true` で参照するため、
順序を逆にすると起動に失敗する。

```bash
cd docker
bin/docker-common.sh up      # 1. 共通インフラ（nginx-proxy / MySQL / Redis / LocalStack）
bin/docker-backends.sh up    # 2. 全10バックエンドを一括起動
```

個別に起動する場合はバックエンドごとのスクリプトを使う（対応表は backend-dispatch Skill）:

```bash
cd docker
bin/docker-go-gin.sh up      # go-gin だけ起動（他は docker-go-echo.sh / docker-php.sh 等）
```

- 1つのバックエンドだけ触る作業では `docker-backends.sh up` を使わず個別起動にする
  （10コンテナのビルドは時間とリソースを消費する）。
- 各 `docker-<x>.sh up` は `docker/local/app-<x>/.env` が無ければ `.env.example` から
  自動生成した上で `docker compose -p auth-<x> up -d --build` を実行する。
- `docker-backends.sh` が受け付ける引数は `up` / `down` のみ（`exec` / `start` / `stop` は
  個別スクリプトを使う）。

### LocalStack の初期化

`docker-common.sh up` は `BACKEND_MODE`（`docker/local/common/.env`）に応じて
compose ファイルを切り替え、`emulator` 以外のモードでは
`docker-localstack-init.sh` を自動実行する。この初期化が
「LocalStackヘルスチェック待ち → Lambda zipビルド → `tflocal apply`（API Gateway /
Lambda / SES / SSM）→ `frontend/.env.local` 生成」までを行う。

| `BACKEND_MODE` | 起動ファイル | 備考 |
|---|---|---|
| `localstack`（既定） | `docker-compose.yml` + `docker-compose.localstack.yml` | 推奨 |
| `localstack-pro` | `docker-compose.yml` + `docker-compose.localstack-pro.yml` | 将来対応 |
| `emulator` | `docker-compose.yml` + `docker-compose.emulator.yml` | 非推奨。LocalStack初期化は走らない |

`down` / `stop` は起動時と同じ `BACKEND_MODE` で実行する（異なるモードだと対象コンテナが
正しく停止されない）。

Terraform定義を変えた場合など手動で再デプロイしたいときのみ:

```bash
cd function && make zip           # Lambda を function.zip にまとめる
cd ../terraform/local && make apply   # 完了時に frontend/.env.local が再生成される
```

## コンテナに入る / 停止する / 破棄する

```bash
docker/bin/docker-php.sh exec    # コンテナ内シェルに入る（Go系とtsは sh、他は bash）
docker/bin/docker-common.sh stop # 共通インフラを停止（破棄しない）
docker/bin/docker-common.sh start
```

`start` / `stop` は `docker-common.sh` のみが対応する。各バックエンドは
`up` / `down` / `exec` の3つだけで、停止相当の操作は `down`（破棄）になる。
再作成したいだけなら `up` を再実行する（`--build` で作り直される）。

## 破壊的操作への注意

- `down` は対象コンテナに加えて **イメージ・ボリュームも削除**する（`--rmi all --volumes`）。
  MySQL のデータも消えるため、`php artisan migrate --seed` からのやり直しが必要になる。
- `docker-common.sh down` はさらに `docker/local/common/data/` と `logs/` を削除し、
  `authorization` ネットワークも削除する（＝全バックエンドが通信不能になる）。
- そのため **`down` はユーザーの明示的な指示がある場合のみ実行し、実行前に一言伝える**
  （`.github/copilot-instructions.md` の方針）。

## トラブルシュート

- **`docker: 'compose' is not a docker command`**
  → ラッパースクリプトは全て Compose v2 の `docker compose` を呼ぶ。Compose v2 プラグイン
  （`docker-compose-plugin`）をインストールする。Compose v1（`docker-compose`）は
  2023年7月にサポート終了しているため対応しない。
- **バックエンドコンテナが起動しない / ネットワークが無いと言われる**
  → `common` が起動しておらず `authorization` ネットワークが無い。
  `docker/bin/docker-common.sh up` を先に実行する（ビルドは先に走り、コンテナ作成の
  段階で external network エラーになるため、失敗まで時間がかかる）。
- **LocalStack初期化が `go: not found` / `tflocal コマンドが見つかりません` で止まる**
  → 初期化はホスト側の `go`（Lambda zipビルド）と `tflocal` を使う。コンテナは起動済みなので、
  ツールを入れてから `cd function && make zip && cd ../terraform/local && make apply` で続行する。
- **イメージビルドが `go install ...@latest` で失敗する**
  → ツールの最新版がイメージのGoより新しいバージョンを要求している場合に起きる
  （例: `dlv@latest` が Go >= 1.25 要求、イメージは Go 1.24）。Dockerfileでバージョンを
  固定するかベースイメージを上げる。既存の不具合なので勝手に直さず報告する。
- **`down` 後に `data/` / `logs/` が残る**
  → コンテナがroot権限で作ったファイルは `rm` できず `Permission denied` になる。
  gitignore対象なので放置して問題ない（消すなら管理者権限が必要）。
- **状態確認**

  ```bash
  docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
  docker compose -p auth-php -f docker/local/app-php/docker-compose.yml logs --tail 100
  ```

- **APIが 502 / 応答しない**: nginx-proxy（`common`）はホスト名でルーティングする。
  バックエンドコンテナが落ちていないか上記 `logs` で確認する。
  なお動作確認は本番同様に Lambda 経由（`http://localhost:3000/function/<backend>/...`）で
  行い、バックエンドを直接叩いてバイパスしない。
- **LocalStack のリソースが無い / API Gateway ID が変わった**
  → `docker/bin/docker-common.sh up` を再実行して初期化を通す。手動なら
  `cd terraform/local && make apply`、`frontend/.env.local` だけ作り直すなら
  `cd terraform/local && make setup-env`（`setup-env` は `terraform/local/Makefile` にしかない）。
- **`.env` が古い / 壊れている**: バックエンド側は `docker/local/app-<x>/.env`。
  削除して `docker/bin/docker-<x>.sh up` を再実行すれば `.env.example` から再生成される
  （設定していた OAuth クライアントID等は再設定が必要）。
- **ポート衝突**: `443`（プロキシ）/ `3306`（MySQL）/ `6379`（Redis）/ `4566`（LocalStack）が
  空いているか確認する。`docker/local/common/.env` で変更可。
- **マイグレーションが無い**: マイグレーションは php-laravel に一本化されている。
  `docker/bin/docker-php.sh exec` → `composer install` → `php artisan migrate --seed` を
  最初に実行する。
- どのバックエンドがどのディレクトリ・スクリプトに対応するかは backend-dispatch Skill を参照。
