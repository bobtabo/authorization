---
name: pr-review-checklist
description: >-
  PRのレビュー（自分のPRの自己点検、CodeRabbit指摘の切り分け、他人のPRのレビュー）を
  行う際に使う。「PRをレビューして」「CodeRabbitの指摘を見て」「移行漏れがないか
  確認して」等の指示で使う。CI待ちや指摘への返信操作は git-branch-strategy Skillを参照。
allowed-tools: Bash(git:*), Bash(gh:*), Bash(rg:*)
---

# pr-review-checklist

フロントエンドのFeature-Based Architecture移行（#140〜#159）で確立したレビュー手法を
手順化したもの。目的は「移動・リファクタで挙動が変わっていないこと」と
「指摘が今回のリグレッションか既存バグかを取り違えないこと」を機械的に担保すること。

## 0. 前提: 差分を把握する

```bash
set -euo pipefail
REPO=bobtabo/authorization
PR=160   # レビュー対象のPR番号に置き換える

gh api "repos/$REPO/pulls/$PR" \
  --jq '"\(.title)\n\(.base.ref) <- \(.head.ref)  +\(.additions)/-\(.deletions)"'
gh api "repos/$REPO/pulls/$PR/files" --paginate --jq '.[].filename'
```

自分のブランチを自己点検する場合:

```bash
set -euo pipefail
git fetch origin develop
git diff --stat origin/develop...HEAD
git diff --name-status origin/develop...HEAD
```

レビュー観点は「差分の行」だけでなく「Issueの受け入れ基準を満たしているか」も含める。
Issue本文（`gh issue view <番号>`）を必ず読んでから差分を見る。

## 1. 移動元コードとの差分突き合わせ（リファクタ・移行PR）

ファイル移動を伴うPRでは、Gitの追加/削除表示だけでは「移動中に混ざった変更」を見落とす。
リネーム検出を明示的に効かせて、純粋な移動と実質的な変更を分離する。

```bash
set -euo pipefail
git fetch origin develop
# リネーム検出（-M）と類似度しきい値を下げて移動を拾う。R100 は内容完全一致の移動
git diff -M --find-copies-harder --name-status -l0 origin/develop...HEAD
```

- `R100` の行は内容完全一致の移動 → レビュー不要。
- `R<100`（例: `R087`）の行は移動しつつ中身が変わっている → **1ファイルずつ中身を確認する**。

```bash
set -euo pipefail
OLD=frontend/components/staff-list.tsx        # 移動元パス
NEW=frontend/features/staffs/components/StaffListPage.tsx   # 移動先パス
# blob 同士を直接比較する（移動元 revision:path と移動先 revision:path）
git diff "origin/develop:$OLD" "HEAD:$NEW" || true
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
LINE_PATTERN="指摘対象の式やシンボル"                    # 指摘箇所を特定できる文字列

# このPRで当該行が変わったか
git diff origin/develop...HEAD -- "$FILE" | rg -n "$LINE_PATTERN" || echo "このPRの差分には無い"
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
git diff --name-only origin/develop...HEAD > /tmp/changed.txt
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
