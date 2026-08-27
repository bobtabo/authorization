---
name: issue-and-pr-workflow
description: >-
  このリポジトリのIssue・ブランチ・コミットメッセージ・PRの命名/記述規約に従って
  作業する際に使う。「Issueを作って」「コミットして」「PRを出して」「Issueを
  クローズして」等の指示、およびクローズ前のチェックリスト更新で使う。
  ブランチライフサイクル・マージ順序・PR後のレビュー監視は git-branch-strategy Skillを参照。
allowed-tools: Bash(git:*), Bash(gh:*)
---

# issue-and-pr-workflow

bobtabo/authorization における Issue → ブランチ → コミット → PR → Issueクローズ
までの「書き方」を統一する手順書。正となる規約は
`.github/copilot-instructions.md` の「ブランチ運用 / Issue 運用 / コミットメッセージ規約」で、
本Skillはそれを実行手順に落としたもの。両者が食い違う場合は
`.github/copilot-instructions.md` を優先し、本Skillを修正する。

## 大前提（勝手にやらないこと）

ユーザーの明示的な指示があるまで、コミット / push / PR作成 / Issueクローズは行わない
（`.github/copilot-instructions.md`「AI コーディングエージェントへの振る舞い方針」）。
本Skillは「指示された後に、どう書くか」を定めるもの。

## 1. Issue

- タイトル: `[auth] <type>: <日本語の概要>`
  - `type` は `feat` / `fix` / `docs` / `refactor` / `chore` / `design`
  - 例: `[auth] feat: issue-and-pr-workflow Skillを作成する`
  - 姉妹リポジトリはプレフィックスが異なる（showcaseは `[showcase]`、mobileは `[mobile]`）。
    このリポジトリでは必ず `[auth]` を使う。
- 本文の構成: `## 背景` → `## やること`（`- [ ]` のチェックリスト）→ `## 受け入れ基準`
  （`- [ ]` のチェックリスト）→ `## 関連`（関連Issue/PR番号）
- 本文中で他のIssueに触れるときは `#160` のように番号で書く（URL直書きでもよいが番号を併記する）

新規Issueは既存Issueの書式を実際に読んでから合わせる:

```bash
set -euo pipefail
NUM=162   # 必須: 参考にしたい既存Issue番号に置き換える
gh issue view "$NUM" --repo bobtabo/authorization --json title,body -q '.title, .body'
```

## 2. ブランチ

- 命名: `feature/issue-<Issue番号>`（バグ修正主体なら `fix/issue-<Issue番号>` も可）
- 派生元: `develop`（`main` から切るのは hotfix のみ）
- Issue番号を含まないブランチ名（`feature/add-skill` 等）は作らない。
  Issueが無い場合は先にIssueを作る。

作成手順・マージ順序は git-branch-strategy Skillを参照。

## 3. コミットメッセージ

- 形式: `<type>(#<Issue番号>): <日本語の概要>`
  - 例: `feat(#162): issue-and-pr-workflow SKILL.md を追加`
  - 複数Issueにまたがる場合は `fix(#86/#87/#88): ...` のように併記する
- `type` はIssueのタイプ規約と同じ（`feat` / `fix` / `docs` / `refactor` / `chore` / `design`）
- 1コミット1目的。機能修正・テスト修正・ドキュメント更新は別コミットに分ける
- push後は該当PRに修正内容のコメントを残す（まとめて後から報告しない）

```bash
set -euo pipefail
# 必須入力: 3つとも対象の値に置き換えてから実行する
NUM=162                # 対象Issue番号
TYPE=feat              # feat|fix|docs|refactor|chore|design
SUMMARY="対応内容を日本語で書く"

git status --short                          # 変更ファイルを確認する
git add path/to/changed_file another/file    # 変更したファイルを列挙する（git add -A / . は使わない）
git commit -m "${TYPE}(#${NUM}): ${SUMMARY}"
```

`git commit` は `.claude/settings.json` のPreToolUseフックで差分の機密情報スキャンを
受ける。`api_key` / `secret` / `password` / `token` への代入らしき行が差分に含まれると
拒否されるため、そのような文字列を含むドキュメントを追加する場合は該当箇所を
見直す（サンプル値でも代入形式を避ける）。

## 4. PR

- base: `develop` / head: 作業ブランチ（`feature/issue-<番号>` または `fix/issue-<番号>`。
  `main` への直接PRは作らない）
- タイトル: コミットメッセージと同形式 `<type>(#<番号>): <日本語の概要>`
- 本文は次の4ブロック:

  ```markdown
  ## Summary

  Issue #<番号> 対応。<何をなぜ変えたかを2〜3行で>

  ## Changes

  - <追加/変更したファイルと内容>

  ## Test plan

  - <実行したコマンドと結果 / 動作確認方法>

  Closes #<番号>
  ```

- Issueの一部だけを対応する場合は `Closes #<番号>` ではなく `Refs #<番号>` にして
  自動クローズを防ぐ。

```bash
set -euo pipefail
# 必須入力: 4つとも対象の値に置き換えてから実行する（例の値のまま実行しない）
NUM=162                # 対象Issue番号
TYPE=feat              # feat|fix|docs|refactor|chore|design
SUMMARY="対応内容を日本語で書く"
BRANCH="feature/issue-${NUM}"   # バグ修正主体なら "fix/issue-${NUM}"。実際の作業ブランチ名に合わせる

# 実行前ガード: 現在のブランチと BRANCH が一致しているか確認する
[ "$(git rev-parse --abbrev-ref HEAD)" = "$BRANCH" ] || { echo "BRANCH が現在のブランチと違います" >&2; exit 1; }

git push -u origin "$BRANCH"
BODY_FILE=$(mktemp)
cat > "$BODY_FILE" <<'EOF'
## Summary

（ここに本文を書く。プレースホルダーのまま作成しない）

## Changes

## Test plan

EOF
printf '\nCloses #%s\n' "$NUM" >> "$BODY_FILE"
gh pr create --repo bobtabo/authorization --base develop --head "$BRANCH" \
  --title "${TYPE}(#${NUM}): ${SUMMARY}" --body-file "$BODY_FILE"
```

本文はヒアドキュメント経由でファイルに書き出してから `--body-file` で渡す
（`--body "$(cat <<EOF ...)"` はMarkdown中のバッククォートやドル記号がシェルに
解釈されて壊れやすい）。

PR作成後のCI待ち・CodeRabbit指摘対応は git-branch-strategy Skill「レビュー監視・対応」を参照。

## 5. Issueクローズ

マージ後に行う。順序を守る（チェックリスト更新 → コメント → クローズ）。

1. Issue本文の `- [ ]` を実態に合わせて `- [x]` に更新する。未対応の項目は
   `- [ ]` のまま残し、残った理由をコメントに書く（実態と違うチェックは付けない）。
2. 対応内容のコメントを付ける。含める情報: 実施内容の要約 / マージされたPRリンク /
   スコープ外だったが見つけた実バグ等の特記事項。
3. クローズする。

```bash
set -euo pipefail
NUM=162   # 必須: 他のIssueを誤って編集しないよう、必ず先に対象Issue番号へ置き換える
BODY_FILE=$(mktemp)

# 1. 本文のチェックリストを更新（取得 → 編集 → 反映）
gh issue view "$NUM" --repo bobtabo/authorization --json body -q .body > "$BODY_FILE"
# ここで $BODY_FILE を編集し、完了した「- [ ]」を「- [x]」に置き換える
# （一括 sed は未完了項目まで [x] にしてしまうため、必ず項目ごとに確認して編集する）
gh issue edit "$NUM" --repo bobtabo/authorization --body-file "$BODY_FILE"
```

```bash
set -euo pipefail
NUM=162   # 必須: 対象Issue番号
PR=170    # 必須: マージ済みPR番号
COMMENT_FILE=$(mktemp)
cat > "$COMMENT_FILE" <<'EOF'
## 対応内容

- （実施内容を箇条書き）

EOF
printf '\nマージ済みPR: https://github.com/bobtabo/authorization/pull/%s\n' "$PR" >> "$COMMENT_FILE"
gh issue comment "$NUM" --repo bobtabo/authorization --body-file "$COMMENT_FILE"
gh issue close "$NUM" --repo bobtabo/authorization
```

`Closes #<番号>` 付きPRがマージされるとGitHubがIssueを自動クローズする。その場合も
本文チェックリストの更新と対応内容コメントは必要（自動クローズは本文を更新しない）。
自動クローズ済みのIssueに追記する場合は `gh issue close` を再実行せず、
`gh issue edit` と `gh issue comment` のみを行う。

## やってはいけないこと

- `develop` / `main` への直接push（必ずfeatureブランチ経由のPR）
- 実態と合わないチェックリスト更新（未対応項目を `- [x]` にする）
- テストを通すためにテスト自体を書き換える（`.github/copilot-instructions.md` の方針）
- 破壊的なgit操作（`git reset --hard` / force push / `git branch -D`）を無許可で行う
  （`.claude/settings.json` のフックが確認を求める）
