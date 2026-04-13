#!/bin/bash
#
# TypeScriptコンテナ環境を操作
#

ARG="${1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "${SCRIPT_DIR}/../local/app-ts"

if [ "${ARG}" = "up" ]; then
    if [ ! -f .env ]; then
        cp .env.example .env
    fi
    docker-compose -p app-ts -f docker-compose.yml up -d --build
elif [ "${ARG}" = "down" ]; then
    docker-compose -p app-ts -f docker-compose.yml down --rmi all --volumes
elif [ "${ARG}" = "exec" ]; then
    docker-compose -p app-ts exec --user 1000 ts sh
else
    echo "使い方: $0 {up|down|exec}"
    exit 1
fi
