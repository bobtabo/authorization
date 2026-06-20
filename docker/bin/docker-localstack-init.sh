#!/bin/bash
#
# LocalStack 初期化スクリプト
# docker-common.sh up 実行後に自動で呼ばれる
#
# 1. LocalStack のヘルスチェック待機
# 2. Lambda 関数の zip ビルド
# 3. tflocal apply（API Gateway / Lambda / SES / SSM 構築）
# 4. frontend/.env.local の自動生成（API Gateway ID 解決）
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

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
if command -v make &>/dev/null && [ -f Makefile ]; then
    make zip
    echo "✅ function.zip 生成完了"
else
    echo "⚠️  make コマンドまたは Makefile が見つかりません。Lambda ビルドをスキップします"
fi

# ── 3. tflocal apply ──
echo ""
echo "🏗️  Terraform リソースを作成中..."
cd "${PROJECT_ROOT}/terraform/local"
if command -v tflocal &>/dev/null; then
    make apply
else
    echo "❌ tflocal コマンドが見つかりません"
    echo "   pip install terraform-local でインストールしてください"
    exit 1
fi

echo ""
echo "================================================"
echo " LocalStack 初期化完了"
echo "================================================"
