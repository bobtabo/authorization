#!/bin/bash
#
# 全バックエンドコンテナ環境を一括操作
#

ARG="${1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

run() {
    bash "${SCRIPT_DIR}/docker-php.sh"    "${ARG}"
    bash "${SCRIPT_DIR}/docker-go.sh"     "${ARG}"
    bash "${SCRIPT_DIR}/docker-python.sh" "${ARG}"
    bash "${SCRIPT_DIR}/docker-ts.sh"     "${ARG}"
}

if [ "${ARG}" = "up" ] || [ "${ARG}" = "down" ]; then
    run
else
    echo "使い方: $0 {up|down}"
    exit 1
fi
