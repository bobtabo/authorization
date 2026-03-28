#!/bin/bash
#
# Go コンテナ環境を起動
#

cd local/app-go
if [ ! -f .env ]; then
    cp .env.example .env
fi
docker-compose -p app-go -f docker-compose.yml up -d --build
