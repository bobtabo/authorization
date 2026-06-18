#!/bin/bash
#
# tflocal apply 後に API Gateway ID を取得し、
# frontend/.env.localstack をテンプレートとして frontend/.env を生成するスクリプト
#
# 使い方:
#   cd terraform/local && bash scripts/setup-env.sh
#   または make setup-env
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
INFRA_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
PROJECT_ROOT="$(cd "${INFRA_DIR}/../.." && pwd)"

TEMPLATE="${PROJECT_ROOT}/frontend/.env.localstack"
OUTPUT="${PROJECT_ROOT}/frontend/.env"

# API Gateway ID を tflocal output から取得
echo "🔍 API Gateway ID を取得中..."
API_GATEWAY_ID=$(cd "${INFRA_DIR}" && tflocal output -raw api_gateway_id 2>/dev/null)

if [ -z "${API_GATEWAY_ID}" ] || [ "${API_GATEWAY_ID}" = "" ]; then
  echo "❌ API Gateway ID を取得できませんでした。"
  echo "   tflocal apply が正常に完了しているか確認してください。"
  exit 1
fi

echo "✅ API Gateway ID: ${API_GATEWAY_ID}"

# テンプレートから .env.local を生成
if [ ! -f "${TEMPLATE}" ]; then
  echo "❌ テンプレートファイルが見つかりません: ${TEMPLATE}"
  exit 1
fi

TMP="$(mktemp)"
LAMBDA_TARGET="http://localhost:4566/restapis/${API_GATEWAY_ID}/local/_user_request_"

if [ -f "${OUTPUT}" ]; then
  # LAMBDA_PROXY_TARGET の行だけ更新する（NEXT_PUBLIC_POSTCODE_API_KEY 等は一切触れない）
  sed "s|LAMBDA_PROXY_TARGET=.*|LAMBDA_PROXY_TARGET=${LAMBDA_TARGET}|g" "${OUTPUT}" > "${TMP}" && mv "${TMP}" "${OUTPUT}"
else
  # 初回のみテンプレートから生成し LAMBDA_PROXY_TARGET を差し込む
  cp "${TEMPLATE}" "${OUTPUT}"
  sed "s|LAMBDA_PROXY_TARGET=.*|LAMBDA_PROXY_TARGET=${LAMBDA_TARGET}|g" "${OUTPUT}" > "${TMP}" && mv "${TMP}" "${OUTPUT}"
  echo "⚠️  初回生成: NEXT_PUBLIC_POSTCODE_API_KEY を ${OUTPUT} に設定してください"
fi

echo "✅ ${OUTPUT} を生成しました"
echo "   NEXT_PUBLIC_API_URL=/function/php/api"
echo "   LAMBDA_PROXY_TARGET=http://localhost:4566/restapis/${API_GATEWAY_ID}/local/_user_request_"
echo ""
echo "⚠️  NEXT_PUBLIC_POSTCODE_API_KEY が未設定の場合は ${OUTPUT} に実際のキーを設定してください"
