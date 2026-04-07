#!/bin/bash
#
# Pythonコンテナ環境を操作
#

ARG="${1}"

cd local/app-python

if [ "${ARG}" = "up" ]; then
    if [ ! -f .env ]; then
        cp .env.example .env
    fi
    docker-compose -p app-python -f docker-compose.yml up -d --build
elif [ "${ARG}" = "down" ]; then
    docker-compose -p app-python -f docker-compose.yml down --rmi all --volumes
elif [ "${ARG}" = "exec" ]; then
    docker-compose -p app-python exec --user 1000 python bash
else
    echo "使い方: $0 {up|down|exec}"
    exit 1
fi