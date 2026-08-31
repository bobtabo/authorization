---
name: pr-review-checklist
description: >-
  PRのレビュー（自分のPRの自己点検、CodeRabbit指摘の切り分け、他人のPRのレビュー）を
  行う際に使う。「PRをレビューして」「CodeRabbitの指摘を見て」「移行漏れがないか
  確認して」等の指示で使う。CI待ちや指摘への返信操作は git-branch-strategy Skillを参照。
allowed-tools: Bash(git:*), Bash(gh:*), Bash(rg:*), Bash(mktemp:*), Bash(cat:*), Bash(rm:*),
  Bash(echo:*)
---

# pr-review-checklist

フロントエンドのFeature-Based Architecture移行（#140〜#159）で確立したレビュー手法を
手順化したもの。目的は「移動・リファクタで挙動が変わっていないこと」と
「指摘が今回のリグレッションか既存バグかを取り違えないこと」を機械的に担保すること。

## 依存Skill

本Skillは以下を参照する。いずれもIssue #166 / #169 で同時期に追加されるため、
それらが `develop` にマージされる前は参照先が存在しない場合がある。その場合は
`.github/copilot-instructions.md`（規約・PR運用）と `.github/workflows/` を直接見る。

| 参照先Skill | 追加Issue | 参照している内容 |
|---|---|---|
| git-branch-strategy | #166 | CI待ち・指摘の抽出と返信操作 |
| backend-ci-trigger | #169 | `feature/issue-*` でのCI発火とCI結果確認 |

## 0. 前提: 差分を把握する

```bash
set -euo pipefail
REPO=bobtabo/authorization
PR=160   # レビュー対象のPR番号に置き換える

gh api "repos/$REPO/pulls/$PR" \
  --jq '"\(.title)\n\(.base.ref) <- \(.head.ref)  +\(.additions)/-\(.deletions)"'
gh api "repos/$REPO/pulls/$PR/files" --paginate --jq '.[].filename'

# 以降の差分コマンドが「今チェックアウトしているブランチ」を見てしまわないよう、
# 対象PRのheadと比較元を専用refに固定する（別ブランチからレビューしても結果が変わらない）
BASE=$(gh api "repos/$REPO/pulls/$PR" --jq '.base.ref')
git fetch origin "pull/${PR}/head:refs/pr/${PR}" "$BASE"
REV="refs/pr/${PR}"        # レビュー対象の内容
BASE_REV="origin/${BASE}"  # 比較元
```

`refs/pr/<番号>` は読み取り専用の確認用ref。**そのPRを自分で修正する場合はこのrefで作業せず、
`head.ref` の実ブランチをcheckoutする**（`git fetch origin <head.ref> && git checkout <head.ref>`）。

自分のブランチを自己点検する場合は、同じ変数を自分のHEADに向ける:

```bash
set -euo pipefail
BASE=develop
git fetch origin "$BASE"
REV=HEAD
BASE_REV="origin/${BASE}"
git diff --stat "$BASE_REV...$REV"
git diff --name-status "$BASE_REV...$REV"
```

以降のコマンドはこの `REV` / `BASE_REV` を使う。

レビュー観点は「差分の行」だけでなく「Issueの受け入れ基準を満たしているか」も含める。
Issue本文（`gh issue view <番号>`）を必ず読んでから差分を見る。

## 1. 移動元コードとの差分突き合わせ（リファクタ・移行PR）

ファイル移動を伴うPRでは、Gitの追加/削除表示だけでは「移動中に混ざった変更」を見落とす。
リネーム検出を明示的に効かせて、純粋な移動と実質的な変更を分離する。

```bash
set -euo pipefail
# -M 単体はGit既定の類似度50%どまりで、50%未満の移動は D + A に分かれて R 行にならない。
# しきい値を明示して下げる（20%）。R100 は内容完全一致の移動
git diff -M20% --find-copies-harder --name-status -l0 "$BASE_REV...$REV"
```

- `R100` の行は内容が完全一致の移動 → **中身の差分レビューは不要**。ただし内容一致は
  「パス参照が壊れていないこと」を保証しない。旧パスへの参照が残っていないかを別途確認する:

  ```bash
  set -euo pipefail
  OLD=frontend/components/staff-list.tsx        # 移動元パス
  # 旧パス（拡張子なしのimport指定も拾う）への参照が残っていないか。
  # import・相対import・ルーティング定義・テスト/設定ファイルのパス指定が対象
  rg -n -F "${OLD%.*}" --glob '!node_modules' \
    || echo "旧パス参照なし"
  ```

  加えて `export` の公開名が変わっていないか、ルーティングが規約でパス解決される仕組み
  （Next.js の `app/` 等）に影響していないかを確認する。
- `R<100`（例: `R087`）の行は移動しつつ中身が変わっている → **1ファイルずつ中身を確認する**。

```bash
set -euo pipefail
OLD=frontend/components/staff-list.tsx        # 移動元パス
NEW=frontend/features/staffs/components/StaffListPage.tsx   # 移動先パス
# blob 同士を直接比較する（移動元 revision:path と移動先 revision:path）。
# パスやリビジョンの打ち間違いを空差分として黙らせないため `|| true` は付けない
git diff "$BASE_REV:$OLD" "$REV:$NEW"
```

確認するポイント:

- 削除されたはずのコードが移動先にあるか（消えたままになっていないか）。
  移動元にしか無いシンボルが残っていないか確認する:

  ```bash
  set -euo pipefail
  SYMBOL=useStaffList   # 移動元にあった関数・定数名に置き換える
  rg -n "$SYMBOL" frontend --glob '!node_modules' || echo "見つかりません（移行漏れの可能性）"
  ```

- 条件式・early return・エラーハンドリングの分岐が同じか（移動中に `if` が消えやすい）
- 依存の向きが規約どおりか。フロントエンドは `features/a` から `features/b` を
  importしていないか、`app/` にロジックが残っていないか（`frontend/AGENTS.md`）
- バックエンドは層の依存方向（`Http → UseCases → Domain ← Infrastructure`）を
  逆流していないか（`.github/copilot-instructions.md`）

## 2. CodeRabbit指摘の切り分け（既存バグ / 今回のリグレッション）

指摘を受けたら、**まず「その挙動はこのPRで変わったのか」を確認する**。判定手順:

```bash
set -euo pipefail
FILE=frontend/features/clients/hooks/useClientForm.ts   # 指摘されたファイル

# 指摘されたコード片はシェルの変数代入に貼らない（' を含むと引用が壊れてそのまま実行されうる）。
# クォートしたヒアドキュメントでファイルに落とし、rg にはデータとして渡す
SNIPPET_FILE=$(mktemp)
cat > "$SNIPPET_FILE" <<'EOF'
array_filter($items)
EOF

# このPRで当該行が変わったか。
# rg -F -f で固定文字列として扱い（コード片の () . $ 等をメタ文字として解釈させない）、
# 追加/削除行（^[+-]）だけを対象にする（未変更のコンテキスト行に当たると誤判定するため。
# diffヘッダの +++/--- は除外する）。
# rg の「不一致(1)」と「エラー(2以上)」を区別する（打ち間違いを『差分に無い』と誤読しないため）。
# git diff はパイプに直結せず先にファイルへ出す（pipefail は最右のrgの 1 を返すので、
# リビジョン・パスの打ち間違いをパイプ内に混ぜると『不一致』に埋もれる）
DIFF_FILE=$(mktemp)
git diff "$BASE_REV...$REV" -- "$FILE" > "$DIFF_FILE"   # 失敗すれば set -e でここで止まる
set +e
rg '^[+-]' "$DIFF_FILE" | rg -v '^(\+\+\+|---)' | rg -F -n -f "$SNIPPET_FILE"
STATUS=$?
set -e
rm -f "$SNIPPET_FILE" "$DIFF_FILE"
case "$STATUS" in
  0) echo "このPRの差分に含まれる（今回のリグレッション候補）" ;;
  1) echo "このPRの差分には無い（既存コードの可能性）" ;;
  *) echo "検索が失敗した（FILE / REV / BASE_REV の指定を確認する）" >&2; exit "$STATUS" ;;
esac
# 移動元も含めて履歴を追う（--follow でリネームを越える）
git log --oneline --follow -5 -- "$FILE"
```

| 判定 | 条件 | 対応 |
|---|---|---|
| 今回のリグレッション | 指摘箇所が本PRの差分に含まれ、`develop` の対応コードと挙動が違う | このPRで修正する |
| 既存バグ | `develop`（または移動元ファイル）に同じ問題が既にある | このPRでは直さない。別Issue化を提案し、返信でそう伝える |
| 指摘が的外れ | 実際のコード・仕様と合っていない（例: 上位で既にガード済み） | 修正せず、根拠を添えて返信する |
| スコープ外 | Issue本文に「対象外」と明記済み、または別Issueで扱う範囲 | 返信でそう伝える |

- **既存バグを勝手に直さない**。指摘元のコードを無断でリバートしない
  （`.github/copilot-instructions.md` の方針）。修正範囲を広げる場合はユーザーの判断を待つ。
- リファクタPRで「既存バグをそのまま移送した」ことが判明した場合は、
  移送は妥当（挙動不変が目的）としつつ、別Issue化する旨をコメントに残す。
- 指摘の抽出・返信のコマンドは git-branch-strategy Skill「3.2 / 3.3」を参照。

## 3. 設計ドキュメントとの整合確認

層構成・依存関係を変える変更では、以下が実装と一致しているかを確認し、ズレていれば
同じPRで更新する（更新しない場合は理由をPR本文に書く）。

| ドキュメント | 更新が必要になる変更 |
|---|---|
| `docs/architecture/backend.puml` | バックエンドの層・コンポーネント構成の追加/変更 |
| `docs/architecture/frontend.puml` | `features/` / `shared/` の構成、hooks/components の役割変更 |
| `docs/api-spec/openapi.yml` | エンドポイント・リクエスト/レスポンスの追加・変更（APIの正） |
| `docs/ui-flow/` | 画面遷移の追加・変更 |
| `frontend/AGENTS.md` | フロントエンドの構成ルール自体の変更 |
| `.github/copilot-instructions.md` | 規約・層構成ルール・Hooks設定の変更 |
| `backends/README.md` / 各 `backends/*/README.md` | バックエンドの追加や起動手順の変更 |

```bash
set -euo pipefail
git diff --name-only "$BASE_REV...$REV" > /tmp/changed.txt
rg -q '^backends/' /tmp/changed.txt && echo "backend.puml / openapi.yml の整合を確認する"
rg -q '^frontend/' /tmp/changed.txt && echo "frontend.puml / AGENTS.md の整合を確認する"
rg -q '^docs/api-spec/' /tmp/changed.txt && echo "OpenAPI変更あり: 全10バックエンドへの波及を確認する"
```

`docs/api-spec/openapi.yml` がAPIの正であり、10バックエンドすべてが同じ仕様を満たす。
1バックエンドだけの仕様変更は実装間の挙動差になるため、意図的かどうかを必ず確認する。

## 4. 共通チェック項目

- Issueの受け入れ基準をすべて満たしているか（満たさない項目はPR本文に明記されているか）
- テストを通すためにテストを書き換えていないか。既存テストの「コメントアウトで無効化」を
  踏襲していないか
- 機密情報（APIキー・トークン・`.env` の実値）が差分に含まれていないか
- 新規バックエンド追加時はロケール／タイムゾーン設定（`Asia/Tokyo` / `ja`）があるか
  （未設定だと9時間ずれる）
- `.env` は `.env` / `.env.example` / `.env.testing` / `.env.testing.local` の4ファイル構成を
  崩していないか
- コミットメッセージ・PRタイトルが規約（`<type>(#<番号>): ...`）に沿っているか
- CIの結果を確認したか（`feature/issue-*` では自動発火しないため backend-ci-trigger Skill参照）

## 5. レビュー結果の残し方

- 自分のPRの自己点検結果は、確認したことをPR本文の Test plan に追記する。
- 他人のPRをレビューする場合、指摘は該当行へのレビューコメントとして残す。
  重大度（must / should / nits）を明示し、既存バグと今回の変更起因を区別して書く。
- スコープ外の実バグを見つけた場合は、そのPRで直させず別Issue化を提案する。
