#!/bin/bash
#
# LocalStack 初期化スクリプト
# docker-common.sh up 実行後に自動で呼ばれる
#
# 0. ホスト側ツールチェーンの事前チェック（go / make / zip / terraform / tflocal）
# 1. LocalStack のヘルスチェック待機
# 2. Lambda 関数の zip ビルド
# 3. tflocal apply（API Gateway / Lambda / SES / SSM 構築）
# 4. frontend/.env.local の自動生成（API Gateway ID 解決）
#
# 使い方:
#   docker-localstack-init.sh          初期化を実行する
#   docker-localstack-init.sh check    ツールの事前チェックのみ行う（docker-common.sh up が
#                                      コンテナ起動前に呼ぶ）
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# ── 0. ホスト側ツールの事前チェック ──
# Lambda zip ビルド（function/Makefile: go build + zip）と tflocal apply はホストのツールを使う。
# 途中で分かりにくく止まらないよう、必要なコマンドをまとめて確認し、無ければ導入手順を案内して終了する。
check_required_tools() {
    local missing=0
    local -a tools=(
        "go|Lambda 関数のビルド（function/Makefile）|https://go.dev/dl/ または brew install go"
        "make|Lambda 関数のビルド / Terraform 操作（Makefile）|apt install make または xcode-select --install"
        "zip|Lambda デプロイ用 function.zip の生成|apt install zip（macOS は標準で利用可）"
        "terraform|LocalStack へのリソース作成（tflocal が内部で呼ぶ）|https://developer.hashicorp.com/terraform/install"
        "tflocal|LocalStack 向け Terraform ラッパー|pip install terraform-local"
        "curl|LocalStack のヘルスチェック（下記手順1）|apt install curl（macOS は標準で利用可）"
    )
    local entry name purpose howto
    for entry in "${tools[@]}"; do
        IFS='|' read -r name purpose howto <<< "${entry}"
        if ! command -v "${name}" &>/dev/null; then
            if [ ${missing} -eq 0 ]; then
                echo "❌ LocalStack 初期化に必要なコマンドが見つかりません:"
            fi
            echo "   - ${name}: ${purpose}"
            echo "       導入方法: ${howto}"
            missing=1
        fi
    done
    if [ ${missing} -ne 0 ]; then
        echo ""
        echo "   上記をインストールしてから、再度 bin/docker-common.sh up を実行してください。"
        echo "   （コンテナが起動済みなら cd function && make zip && cd ../terraform/local && make apply でも続行できます）"
        return 1
    fi
    return 0
}

if [ "${1:-}" = "check" ]; then
    check_required_tools
    exit $?
fi

# docker-common.sh up はコンテナ起動前に check を済ませているので再チェックしない
if [ "${1:-}" != "--skip-check" ]; then
    check_required_tools
fi

# .env からコンテナ名を読み込む
ENV_FILE="${SCRIPT_DIR}/../local/common/.env"
if [ -f "${ENV_FILE}" ]; then
    source "${ENV_FILE}"
fi
LOCALSTACK_CONTAINER="${LOCALSTACK_CONTAINER:-auth-localstack}"

echo ""
echo "================================================"
echo " LocalStack 初期化"
echo "================================================"

# ── 1. LocalStack ヘルスチェック ──
echo ""
echo "⏳ LocalStack の起動を待機中..."
MAX_WAIT=60
ELAPSED=0
while [ ${ELAPSED} -lt ${MAX_WAIT} ]; do
    # コンテナが停止済みなら即終了
    if ! docker ps --format '{{.Names}}' | grep -q "^${LOCALSTACK_CONTAINER}$" 2>/dev/null; then
        echo "❌ LocalStack コンテナが停止しています。ログを確認してください:"
        echo "   docker logs ${LOCALSTACK_CONTAINER}"
        exit 1
    fi
    if curl -sf http://localhost:4566/_localstack/health > /dev/null 2>&1; then
        echo "✅ LocalStack 起動完了"
        break
    fi
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    echo "   待機中... (${ELAPSED}s/${MAX_WAIT}s)"
done

if [ ${ELAPSED} -ge ${MAX_WAIT} ]; then
    echo "❌ LocalStack の起動がタイムアウトしました（${MAX_WAIT}秒）"
    echo "   手動で起動を確認し、cd terraform/local && make apply を実行してください"
    exit 1
fi

# ── 2. Lambda zip ビルド ──
echo ""
echo "📦 Lambda 関数をビルド中..."
cd "${PROJECT_ROOT}/function"
if [ ! -f Makefile ]; then
    echo "❌ ${PROJECT_ROOT}/function/Makefile が見つかりません。リポジトリのチェックアウトを確認してください"
    exit 1
fi
make zip
echo "✅ function.zip 生成完了"

# ── 3. tflocal apply ──
echo ""
echo "🏗️  Terraform リソースを作成中..."
cd "${PROJECT_ROOT}/terraform/local"
make apply

echo ""
echo "================================================"
echo " LocalStack 初期化完了"
echo "================================================"
