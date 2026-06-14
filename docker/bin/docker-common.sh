#!/bin/bash
#
# 共通コンテナ環境を操作
#
# BACKEND_MODE（.env で設定）に応じて起動する Docker Compose ファイルを切り替える。
#   localstack     : LocalStack Community（デフォルト）
#   localstack-pro : LocalStack Pro（将来対応）
#   emulator       : Lambda 常駐起動 + MailPit（非推奨）
#

ARG="${1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "${SCRIPT_DIR}/../local/common"

# .env から BACKEND_MODE を読み込む
if [ -f ./.env ]; then
    source ./.env
fi
BACKEND_MODE="${BACKEND_MODE:-localstack}"

# モードに応じた compose ファイルを決定
compose_files() {
    case "${BACKEND_MODE}" in
        localstack-pro)
            echo "-f docker-compose.yml -f docker-compose.localstack-pro.yml"
            ;;
        emulator)
            echo "-f docker-compose.yml -f docker-compose.emulator.yml"
            ;;
        *)
            # デフォルト: localstack
            echo "-f docker-compose.yml -f docker-compose.localstack.yml"
            ;;
    esac
}

if [ "${ARG}" = "up" ]; then
    docker network create --driver bridge authorization 2>/dev/null || true
    eval "docker compose $(compose_files) up -d --build"
    # LocalStack モード時のみ初期化スクリプトを実行
    if [ "${BACKEND_MODE}" != "emulator" ]; then
        "${SCRIPT_DIR}/docker-localstack-init.sh"
    fi
elif [ "${ARG}" = "down" ]; then
    eval "docker compose $(compose_files) down --rmi all --volumes"
    docker network rm authorization 2>/dev/null || true
    rm -fdR data
    rm -fdR logs
elif [ "${ARG}" = "start" ]; then
    eval "docker compose $(compose_files) start"
elif [ "${ARG}" = "stop" ]; then
    eval "docker compose $(compose_files) stop"
elif [ "${ARG}" = "env" ]; then
    cp ./environment/default.crt ./proxy/certs/default.crt
    cp ./environment/default.key ./proxy/certs/default.key
    cp ./environment/.env.example ./.env
else
    echo "使い方: $0 {up|down|start|stop|env}"
    echo ""
    echo "BACKEND_MODE (現在: ${BACKEND_MODE}):"
    echo "  localstack     - LocalStack Community（デフォルト）"
    echo "  localstack-pro - LocalStack Pro（将来対応）"
    echo "  emulator       - Lambda 常駐 + MailPit（非推奨）"
    exit 1
fi
