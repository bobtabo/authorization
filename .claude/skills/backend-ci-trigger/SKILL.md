---
name: backend-ci-trigger
description: >-
  feature/issue-* ブランチではバックエンドCIが自動実行されないため、変更内容から
  対象ワークフローを判断して手動発火・結果確認する際に使う。「CIを回して」
  「CIの結果を確認して」等の指示で使う。
allowed-tools: Bash(gh:*), Bash(git:*), Bash(sort:*), Bash(comm:*), Bash(grep:*), Bash(printf:*),
  Bash(seq:*), Bash(sleep:*), Bash(echo:*)
---

# backend-ci-trigger

`.github/workflows/*-ci.yml` の各CIは次の2点により、`feature/issue-*` ブランチへの
pushでは**自動実行されない**。

1. `paths` トリガーが対応するディレクトリのみに限定されている
2. `branches-ignore: ['feature/issue-*']` により `feature/issue-*` へのpush自体が対象外

`feature/issue-*` 以外のブランチ（develop 等）へのpushでは、該当 `paths` に応じて自動実行される。
`feature/issue-N` 上でCI結果を見たい場合は `workflow_dispatch` で手動発火する。

例外: `docs-ci.yml` は `on: pull_request`（ブランチ・パス制限なし）のため、すべてのPRで
自動実行される（OpenAPI仕様の検証）。手動発火は不要。

## ワークフロー対応表

| ワークフローファイル | 対象パス | 自動発火の条件 |
|---|---|---|
| `go-ci.yml` | `backends/go-gin/**` | push（`feature/issue-*` 以外） |
| `go-echo-ci.yml` | `backends/go-echo/**` | 同上 |
| `go-beego-ci.yml` | `backends/go-beego/**` | 同上 |
| `kotlin-ci.yml` | `backends/kotlin-ktor/**` | 同上 |
| `php-ci.yml` | `backends/php-laravel/**` | 同上 |
| `python-ci.yml` | `backends/python-fastapi/**` | 同上 |
| `ruby-hanami-ci.yml` | `backends/ruby-hanami/**` | 同上 |
| `ruby-rails-ci.yml` | `backends/ruby-rails/**` | 同上 |
| `rust-ci.yml` | `backends/rust-axum/**` | 同上 |
| `ts-ci.yml` | `backends/ts-hono/**` | 同上 |
| `frontend-e2e-ci.yml` | `frontend/**` | 同上 |
| `docs-ci.yml` | 制限なし | すべてのPR（+ `main` へのpush）。`lint-openapi` / `deploy-pages` |

- go-gin のワークフローは `go-gin-ci.yml` ではなく **`go-ci.yml`**、kotlin-ktor は
  **`kotlin-ci.yml`** のように、ファイル名はディレクトリ名と一致しない。必ず上表を引く。
- `gh workflow run` にはワークフロー表示名ではなく上表のファイル名を使う（曖昧さがない）。
- `gh workflow run` が `HTTP 403: Resource not accessible by integration` になる場合、使っている
  トークンに `actions: write` が無い。この場合は手動発火を諦め、GitHub UI
  （Actions → 対象ワークフロー → Run workflow → 対象ブランチ）での実行をユーザーに依頼するか、
  PRの `docs-ci.yml` の結果とマージ後の自動発火で確認する。
- `workflow_dispatch` はワークフローファイルがdefaultブランチ（`main`）に存在する場合のみ
  使える。新規追加したワークフローは `main` に入るまで手動発火できないため、その場合は
  「CI未実行（develop→main反映後に確認）」と明示してユーザーに報告する。

## 発火対象の判断

```bash
set -euo pipefail
git fetch origin develop
git diff --name-only origin/develop...HEAD
```

- 出力に `backends/<name>/**` があれば、上表で対応するワークフローのみ発火する。
- `frontend/**` があれば `frontend-e2e-ci.yml` を発火する。
- `.claude/**`、`docs/**`、`README.md` のみの変更ならバックエンドCIの発火は不要
  （PRで `docs-ci.yml` が動く）。
- `docker/**`・`function/**`・`terraform/**`・`.github/workflows/**` など特定バックエンドに
  閉じない変更が含まれる場合は、影響範囲を推測で切り分けず、上表のバックエンド10本を
  すべて発火するフォールバックを取る。

## 手動発火と結果確認

`gh workflow run` は成功時にrun URLを返さないことがある。発火前に存在したrunの
`databaseId` 集合を記録し、そこに含まれない新しいIDを対象runとして特定する
（`createdAt` は秒精度しかなく、同じ秒の既存runと誤って一致しうる）。
`gh run list` は既定で20件しか返さないため `--limit` を明示する。

```bash
set -euo pipefail
WORKFLOW=php-ci.yml    # 上表のワークフローファイル名に置き換える
BRANCH=feature/issue-169   # 対象ブランチに置き換える
REPO=bobtabo/authorization

BEFORE=$(gh run list --repo "$REPO" --workflow="$WORKFLOW" --branch="$BRANCH" \
  --event=workflow_dispatch --limit 100 --json databaseId --jq '.[].databaseId' | sort -u)

gh workflow run "$WORKFLOW" --repo "$REPO" --ref "$BRANCH"

RUN_ID=""
for i in $(seq 1 20); do
  AFTER=$(gh run list --repo "$REPO" --workflow="$WORKFLOW" --branch="$BRANCH" \
    --event=workflow_dispatch --limit 100 --json databaseId --jq '.[].databaseId' | sort -u)
  NEW=$(comm -13 <(printf '%s\n' "$BEFORE") <(printf '%s\n' "$AFTER") | grep . || true)
  COUNT=$(printf '%s\n' "$NEW" | grep -c . || true)
  if [ "$COUNT" -eq 1 ]; then
    RUN_ID="$NEW"
    break
  elif [ "$COUNT" -gt 1 ]; then
    echo "runの候補が複数あり一意に特定できません。手動でrun IDを確認してください" >&2
    exit 1
  fi
  sleep 3
done
[ -n "$RUN_ID" ] || { echo "新しいrunを検出できませんでした" >&2; exit 1; }
echo "run: https://github.com/${REPO}/actions/runs/${RUN_ID}"
```

完了を待って結果を確認する（別プロセスになるため `RUN_ID` を再設定する）。
`gh run watch` はフォアグラウンドを長時間占有するので、Bashツールでは
`run_in_background: true` で走らせて完了通知を待つ:

```bash
set -euo pipefail
REPO=bobtabo/authorization
RUN_ID=1234567890   # 上で得たrun IDに置き換える

set +e
gh run watch --repo "$REPO" "$RUN_ID" --exit-status
STATUS=$?
set -e
if [ "$STATUS" -ne 0 ]; then
  # 失敗したジョブのログだけを見る（--log は全ジョブ分が出て非常に長い）
  gh run view --repo "$REPO" "$RUN_ID" --log-failed || \
    echo "警告: ログ取得に失敗しました（runの結果には影響しません）" >&2
fi
exit "$STATUS"
```

PRのチェック状況をまとめて見たい場合:

```bash
set -euo pipefail
REPO=bobtabo/authorization
BRANCH=feature/issue-169   # 対象ブランチに置き換える

# check-runs の ref にはブランチ名をそのまま渡せる（SHA解決の往復は不要）
gh api "repos/${REPO}/commits/${BRANCH}/check-runs" \
  --jq '.check_runs[] | "\(.name): \(.status) \(.conclusion // "")"'
```

## 注意

- 各バックエンドCIは MySQL / Redis のサービスコンテナを立ち上げるため、1本あたり数分かかる。
  必要なワークフローだけを発火する。
- `frontend-e2e-ci.yml` は Playwright のモック版（chromium）のみを実行する。
  `real-*` プロジェクト（LocalStack + Lambda が必要）と `frontend/e2e/demo/`（GIF録画用）は
  CIでは実行されない。
- CIがグリーンでも、featureブランチで発火したrunはPRのチェック欄に紐づかない場合がある。
  その場合はrunのURLをPRコメントに残して報告する。
