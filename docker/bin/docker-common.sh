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
cd "${SCRIPT_DIR}/../local/common" || { echo "❌ ディレクトリへの移動に失敗しました: ${SCRIPT_DIR}/../local/common" >&2; exit 1; }

# .env から BACKEND_MODE を読み込む
if [ -f ./.env ]; then
    source ./.env
fi
BACKEND_MODE="${BACKEND_MODE:-localstack}"

# モードに応じた compose コマンドを組み立て
compose_cmd=(docker compose -f docker-compose.yml)
case "${BACKEND_MODE}" in
    localstack-pro)
        compose_cmd+=(-f docker-compose.localstack-pro.yml)
        ;;
    emulator)
        compose_cmd+=(-f docker-compose.emulator.yml)
        ;;
    localstack)
        compose_cmd+=(-f docker-compose.localstack.yml)
        ;;
    *)
        echo "❌ 不明な BACKEND_MODE: ${BACKEND_MODE}" >&2
        echo "   使用可能な値: localstack | localstack-pro | emulator" >&2
        exit 1
        ;;
esac

if [ "${ARG}" = "up" ]; then
    # LocalStack モード時は初期化に使うホスト側ツールをコンテナ起動前に確認する
    if [ "${BACKEND_MODE}" != "emulator" ]; then
        "${SCRIPT_DIR}/docker-localstack-init.sh" check || exit 1
    fi
    docker network create --driver bridge authorization 2>/dev/null || true
    "${compose_cmd[@]}" up -d --build
    # LocalStack モード時のみ初期化スクリプトを実行
    if [ "${BACKEND_MODE}" != "emulator" ]; then
        "${SCRIPT_DIR}/docker-localstack-init.sh" --skip-check
    fi
elif [ "${ARG}" = "down" ]; then
    "${compose_cmd[@]}" down --rmi all --volumes
    docker network rm authorization 2>/dev/null || true
    # data/ logs/ にはコンテナ内プロセス（root 等）が書き込んだファイルが残るため、
    # ホストの rm では Permission denied になりうる。使い捨てコンテナで root として中身を削除する。
    # ディレクトリ以外（通常ファイル・シンボリックリンク）はホスト側でそのまま消せる。
    mounts=()
    for target in data logs; do
        if [ -L "${target}" ] || { [ -e "${target}" ] && [ ! -d "${target}" ]; }; then
            rm -f "${target}"
        elif [ -d "${target}" ]; then
            mounts+=(-v "$(pwd)/${target}:/target/${target}")
        fi
    done
    if [ ${#mounts[@]} -gt 0 ]; then
        if docker run --rm "${mounts[@]}" alpine:3.21 \
            sh -c 'find /target -mindepth 2 -delete'; then
            rmdir data logs 2>/dev/null
        else
            echo "⚠️  使い捨てコンテナ（alpine:3.21）での削除に失敗したため、ホスト側で削除を試みます" >&2
        fi
        if ! rm -fdR data logs; then
            echo "❌ data/ logs/ を削除できませんでした。手動で削除してください:" >&2
            echo "   sudo rm -rf $(pwd)/data $(pwd)/logs" >&2
            exit 1
        fi
    fi
elif [ "${ARG}" = "start" ]; then
    "${compose_cmd[@]}" start
elif [ "${ARG}" = "stop" ]; then
    "${compose_cmd[@]}" stop
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
