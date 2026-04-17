#!/bin/bash
#
# Ruby（Ruby On Rails）コンテナ環境を操作
#

ARG="${1}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "${SCRIPT_DIR}/../local/app-rb-rails"

if [ "${ARG}" = "up" ]; then
    if [ ! -f .env ]; then
        cp .env.example .env
    fi
    docker-compose -p app-rb-rails -f docker-compose.yml up -d --build
elif [ "${ARG}" = "down" ]; then
    docker-compose -p app-rb-rails -f docker-compose.yml down --rmi all --volumes
elif [ "${ARG}" = "exec" ]; then
    docker-compose -p app-rb-rails exec --user 1000 rb-rails bash
else
    echo "使い方: $0 {up|down|exec}"
    exit 1
fi
