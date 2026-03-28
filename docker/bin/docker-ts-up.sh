#!/bin/bash
#
# TypeScript コンテナ環境を起動
#

cd local/app-ts
if [ ! -f .env ]; then
    cp .env.example .env
fi
docker-compose -p app-ts -f docker-compose.yml up -d --build
