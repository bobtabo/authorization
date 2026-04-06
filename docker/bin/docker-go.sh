#!/bin/bash
#
# Goコンテナ環境を操作
#

ARG="${1}"

cd local/app-go

if [ "${ARG}" = "up" ]; then
    if [ ! -f .env ]; then
        cp .env.example .env
    fi
    docker-compose -p app-go -f docker-compose.yml up -d --build
elif [ "${ARG}" = "down" ]; then
    docker-compose -p app-go -f docker-compose.yml down --rmi all --volumes
elif [ "${ARG}" = "exec" ]; then
    docker-compose -p app-go exec --user 1000 go sh
else
    echo "使い方: $0 {up|down|exec}"
    exit 1
fi