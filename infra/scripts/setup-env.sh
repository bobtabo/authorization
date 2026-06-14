#!/bin/bash
#
# tflocal apply 後に API Gateway ID を取得し、
# frontend/.env.localstack をテンプレートとして frontend/.env.local を生成するスクリプト
#
# 使い方:
#   cd infra && bash scripts/setup-env.sh
#   または make setup-env
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
INFRA_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
PROJECT_ROOT="$(cd "${INFRA_DIR}/.." && pwd)"

TEMPLATE="${PROJECT_ROOT}/frontend/.env.localstack"
OUTPUT="${PROJECT_ROOT}/frontend/.env.local"

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

cp "${TEMPLATE}" "${OUTPUT}"
TMP="$(mktemp)"
sed "s/{api-id}/${API_GATEWAY_ID}/g" "${OUTPUT}" > "${TMP}" && mv "${TMP}" "${OUTPUT}"

echo "✅ ${OUTPUT} を生成しました"
echo "   NEXT_PUBLIC_API_URL=http://localhost:4566/restapis/${API_GATEWAY_ID}/local/_user_request_/function/php/api"
