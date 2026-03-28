#!/bin/bash
#
# Python コンテナ環境を起動
#

cd local/app-python
if [ ! -f .env ]; then
    cp .env.example .env
fi
docker-compose -p app-python -f docker-compose.yml up -d --build
