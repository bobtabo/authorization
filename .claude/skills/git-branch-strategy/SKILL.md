---
name: git-branch-strategy
description: >-
  Git-Flow に沿ったブランチ運用（feature/issue-xxx → develop、release/* → main、
  hotfix/*）でブランチを切る・PRを作る・PR後のCI待ちとCodeRabbit指摘対応を行う際に使う。
  「ブランチを切って」「PRを出して」「レビュー指摘に対応して」等の指示で使う。
  Issue/コミット/PRの文面の書き方は issue-and-pr-workflow Skillを参照。
allowed-tools: Bash(git:*), Bash(gh:*), Bash(jq:*), Bash(awk:*), Bash(sort:*), Bash(comm:*),
  Bash(tail:*), Bash(printf:*), Bash(sleep:*), Bash(seq:*)
---

# git-branch-strategy

bobtabo/authorization のブランチライフサイクルとマージ順序、PR後のレビュー監視までを
統一する手順書。命名規則・コミットメッセージ・PR本文の書式は
issue-and-pr-workflow Skillが正。本Skillは「どのブランチからどこへ、どの順で流すか」と
「PRを出したあと何を繰り返すか」を定める。

## 依存Skill（マージ順）

本Skillは以下を参照する。いずれも #162〜#169 で同時期に追加されるため、
それらが `develop` にマージされる前は参照先が存在しない場合がある（段階的導入）。

| 参照先Skill | 追加Issue / PR | 参照している内容 | 未マージ時の代替 |
|---|---|---|---|
| issue-and-pr-workflow | #162 / PR #170 | Issue・コミット・PRの書式 | `.github/copilot-instructions.md` |
| pr-review-checklist | #165 / PR #173 | 指摘の切り分け基準 | `.github/copilot-instructions.md` |
| backend-ci-trigger | #169 / PR #177 | CIの手動発火とログ確認 | `.github/workflows/*.yml` を直接読む |

参照を全て解消した状態で入れたい場合は、#170 / #173 / #177 を本SkillのPR（#174）より
先にマージする。

## ブランチライフサイクル

| ブランチ | 派生元 | マージ先 | 用途 |
|---|---|---|---|
| `develop` | - | `main`（リリース時） | 開発の統合ブランチ。ここを基準に作業する |
| `feature/issue-<N>` | `develop` | `develop` | Issue単位の通常作業（バグ修正主体なら `fix/issue-<N>` も可） |
| `release/<version>` | `develop` | `main`（および `develop` へ戻す） | リリース準備 |
| `hotfix/issue-<N>` | `main` | `main` および `develop` | 本番の緊急修正 |
| `main` | - | - | リリース済みの状態 |

- `develop` / `main` への直接pushは禁止。必ずPR経由。
- **`feature/` / `fix/` / `hotfix/` ブランチはIssue番号を含まない名前を作らない**
  （`feature/add-skill` などは不可。Issueがないなら先にIssueを作る）。
  `release/<version>` はリリース単位のブランチなのでIssue番号を含まない（例外）。
- 作業前に必ず派生元を最新化する（下記手順1）。

## マージ順序のルール

下記 2 / 3 は「`main` を含む流れの全体像」であり、**エージェントが自動実行してよい
手順ではない**。`main` へのマージとその後の戻し作業は、ユーザーに提案し、
明示的な指示を得てから実行する（ルール4を優先する）。

1. `feature/issue-<N>` → `develop`（PR）。複数のfeatureがある場合は依存関係の下位から順に
   マージし、後続ブランチは `develop` を取り込んでから進める。
2. リリース時は `release/<version>` を `develop` から切って `main` へPR。
   `main` へマージしたら **`main` の内容を `develop` にも戻す**（リリース中に `main` 側で
   入った修正を `develop` が失わないようにする）。
   → エージェントはこの手順を**提案するまで**。PR作成・マージは指示待ち。
3. `hotfix/issue-<N>` は `main` から切り、`main` へマージした後、必ず `develop` にも取り込む。
   `develop` への取り込みを忘れると次のリリースで修正が消える。
   → `main` からのブランチ作成と修正・pushまではエージェントが行ってよい。
   **`main` へのマージと `develop` への取り込みは、必要だと伝えた上でユーザーの指示を待つ**。
4. **`develop` → `main` のマージ、および `main` へのマージをエージェントが実行しない**。
   リリース判断はユーザーが行うため、明示的な指示を待つ（自動では実行しない）。
   このルールは 2 / 3 の記述より強い。迷ったら実行せずに確認する。

## 1. featureブランチ作成

```bash
set -euo pipefail
REPO=bobtabo/authorization
BASE=develop        # hotfix の場合のみ main
NUM=166             # 対応するIssue番号に置き換える

gh issue view "$NUM" --repo "$REPO" --json title --jq .title   # Issueの存在確認（無ければここで停止）
git checkout "$BASE"
git pull --ff-only origin "$BASE"
git checkout -b "feature/issue-${NUM}"
```

`set -euo pipefail` により、Issueが存在せず `gh issue view` が失敗した場合は
`git checkout -b` を実行せずに停止する。`git pull` は `--ff-only`（ローカルとリモートが
分岐している場合は自動マージせず停止）。

## 2. develop へのPR作成

base は常に `develop`（hotfixのみ `main`）。タイトル・本文の書式と `gh pr create` の
実行例は issue-and-pr-workflow Skill「4. PR」を参照。

## 3. レビュー監視・対応（CI / CodeRabbit）

PRを作ったら、CIとCodeRabbitレビューが収束するまで監視する。人間レビュアーの承認を
待つ／催促するのは対象外（ユーザーの判断に委ねる）。
以下の各ブロックは独立したシェルプロセスとして実行される前提で自己完結させている。
PR番号はハードコードせず毎回ブランチ名から解決する（決め打ちすると別PRを誤操作しうる）。

### 3.1 CI待ち

`backends/**` や `frontend/**` を変更している場合、`feature/issue-*` へのpushでは
CIが自動実行されない（`branches-ignore`）。backend-ci-trigger Skillで対象ワークフローを
`workflow_dispatch` で発火してから待つ。`docs-ci.yml` はPRで常に自動実行される。

固定 `sleep` でフォアグラウンドをブロックしない。Bashツールの `run_in_background: true`
でポーリングし、完了を待つ。全チェックの状態を集約し、失敗した `conclusion` が1件でもあれば
即停止する（先頭のチェックだけを見ない）。API取得失敗・タイムアウトも失敗として扱う:

```bash
set -euo pipefail
REPO=bobtabo/authorization
BRANCH=feature/issue-166   # 対象ブランチに置き換える
PR=$(gh pr list --repo "$REPO" --head "$BRANCH" --state open --json number --jq '.[0].number')
SHA=$(gh api "repos/${REPO}/pulls/${PR}" --jq .head.sha)

for i in $(seq 1 40); do
  # name ごとに started_at が最新の1件だけを見る。
  # 「Re-run failed jobs」では同じSHAに古い試行の failure が残るため、
  # 全件をそのまま見ると再実行が成功しても stale な失敗を拾ってしまう。
  RESULT=$(gh api "repos/${REPO}/commits/${SHA}/check-runs?per_page=100" --paginate --slurp \
    | jq -r '[.[].check_runs[]] | group_by(.name) | map(max_by(.started_at))
             | .[] | "\(.name)\t\(.status)\t\(.conclusion // "-")"') \
    || { echo "check-runs の取得に失敗しました" >&2; exit 1; }
  # タブ区切りの判定は awk で行う（GNU grep の正規表現では `\t` がタブにならない）。
  if printf '%s\n' "$RESULT" \
    | awk -F'\t' '$3 ~ /^(failure|timed_out|cancelled|action_required)$/ {f=1} END {exit !f}'; then
    echo "CIが失敗しました:"; printf '%s\n' "$RESULT"; exit 1
  fi
  if [ -n "$RESULT" ] && printf '%s\n' "$RESULT" | awk -F'\t' '$2 != "completed" {exit 1}'; then
    echo "全チェック完了:"; printf '%s\n' "$RESULT"; break
  fi
  echo "[$i] 実行中"; printf '%s\n' "$RESULT"
  if [ "$i" -eq 40 ]; then echo "タイムアウト: CIが完了しませんでした" >&2; exit 1; fi
  sleep 15
done
```

`--slurp` は `gh api --jq` と併用できないので、パイプで `jq` に渡す（ページをまとめて
から name ごとに集約するため、ページ境界で同名の試行が分かれても正しく最新を取れる）。

`conclusion` が `skipped` / `neutral` / `success` は許容（`skipped` は path フィルタで
対象外だった場合に出る）。`failure` / `timed_out` / `cancelled` / `action_required` は失敗として扱う。

失敗したCIのログ確認は backend-ci-trigger Skill を参照。

### 3.2 CodeRabbitの未返信指摘を抽出する

「未返信」とは `coderabbitai[bot]` のtop-levelコメントのうち、bot以外からの返信が
付いていないもの。top-levelコメント数をそのまま数えると返信済みも数えてしまう。
レビューコメント（インライン）とissueコメント（PR全体宛）の両方を見る:

```bash
set -euo pipefail
REPO=bobtabo/authorization
BRANCH=feature/issue-166   # 対象ブランチに置き換える
PR=$(gh pr list --repo "$REPO" --head "$BRANCH" --state open --json number --jq '.[0].number')

# インラインのレビューコメント（未返信のIDを出力）
TOP=$(gh api "repos/${REPO}/pulls/${PR}/comments" --paginate \
  --jq '.[] | select(.user.login=="coderabbitai[bot]" and .in_reply_to_id==null) | .id' | sort -u)
REPLIED=$(gh api "repos/${REPO}/pulls/${PR}/comments" --paginate \
  --jq '.[] | select(.in_reply_to_id != null and .user.login!="coderabbitai[bot]") | .in_reply_to_id' | sort -u)
comm -23 <(printf '%s\n' "$TOP") <(printf '%s\n' "$REPLIED")

# PR全体宛のレビューサマリ（actionable comments の件数を確認する）
gh api "repos/${REPO}/issues/${PR}/comments" --paginate \
  --jq '.[] | select(.user.login=="coderabbitai[bot]") | "\(.created_at) \(.html_url)"' | tail -5
```

API応答は `gh` の `--jq` で直接フィルタする（応答をシェル変数に入れて `echo | jq` に
通すと、本文中のエスケープシーケンスが `echo` で壊れてjqがパースエラーになることがある）。

個別コメントの本文確認:

```bash
set -euo pipefail
REPO=bobtabo/authorization
COMMENT_ID=1234567   # 3.2で抽出したID
gh api "repos/${REPO}/pulls/comments/${COMMENT_ID}" --jq '"\(.path):\(.line)\n\(.body)"'
```

### 3.3 指摘への対応と返信

未返信の指摘ごとに:

1. 指摘内容を現在のコードと照らして検証する。「既存バグか今回のリグレッションか」の
   切り分け基準は pr-review-checklist Skill を参照。
2. 妥当な指摘は修正してコミット・push（コミットメッセージは issue-and-pr-workflow Skill の
   規約に従う）。既に対応済み／的外れ／スコープ外（Issue本文に明記済み等）なら修正せず
   理由を添えて返信する。スコープ外の実バグを見つけた場合は勝手に直さず、別Issue化を
   提案してユーザーの判断を待つ。
3. コメントIDごとに返信する:

```bash
set -euo pipefail
REPO=bobtabo/authorization
BRANCH=feature/issue-166
PR=$(gh pr list --repo "$REPO" --head "$BRANCH" --state open --json number --jq '.[0].number')
COMMENT_ID=1234567                          # 3.2で抽出した未返信コメントID
COMMIT_SHA=$(git rev-parse --short HEAD)
FIX_SUMMARY="何をどう直したかを記述する"    # プレースホルダーのまま送信しない

gh api "repos/${REPO}/pulls/${PR}/comments/${COMMENT_ID}/replies" \
  -f body="対応しました（${COMMIT_SHA}）。${FIX_SUMMARY}"
```

### 3.4 再レビューのトリガーと収束判定

CodeRabbitは短時間に複数コミットが積まれると "reviews paused" になり、pushだけでは
新しいレビューが走らないことがある。その場合は明示的に再トリガーする:

```bash
set -euo pipefail
REPO=bobtabo/authorization
BRANCH=feature/issue-166
PR=$(gh pr list --repo "$REPO" --head "$BRANCH" --state open --json number --jq '.[0].number')
gh pr comment "$PR" --repo "$REPO" --body "@coderabbitai review"
```

再トリガー後、3.1のCI待ちと3.2の未返信抽出を繰り返す。収束（完了）の判定は次の**両方**:

- 3.2の未返信コメントが0件（＝新規 actionable comment が0件）
- `gh api repos/bobtabo/authorization/pulls/<PR番号> --jq '"\(.mergeable) \(.mergeable_state)"'` が
  `true clean`（GraphQL の `mergeable: MERGEABLE` / `mergeStateStatus: CLEAN` と同等）

目安として5ラウンド前後で収束しない場合は無限にループさせず打ち切り、未解決点を添えて
ユーザーに判断を仰ぐ。収束したら最終状態（CI結果・対応した指摘の要約・PRリンク）を報告する。

**developへのマージはこのSkillでは行わない**（ユーザーの明示的な指示を待つ）。
マージ後のIssueクローズ手順は issue-and-pr-workflow Skill「5. Issueクローズ」を参照。

## 禁止事項

- `develop` / `main` への直接push
- `develop` → `main`、および `main` へのマージをユーザーの指示なく行うこと
- 無許可の破壊的操作（`git reset --hard` / force push / `git branch -D` / `git clean -f`）。
  `.claude/settings.json` のPreToolUseフックが検出して確認を求める
- `git add -A` / `git add .` による一括ステージ（意図しないファイルを混ぜ込む）

## 既知の制約

本SkillはSKILL.md（手順書）であり、`develop` / `main` への直接pushを機械的にブロックしない。
`.claude/settings.json` のフックが対象とするのは force push / `reset --hard` /
`branch -D` / `clean -f` などの破壊的操作のみ。権限層で強制したい場合はGitHub側の
ブランチ保護ルール設定を別Issueとして検討する。
