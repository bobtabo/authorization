#!/bin/bash
#
# PHP コンテナ環境を起動
#

cd local/app-php
if [ ! -f .env ]; then
    cp .env.example .env
fi
docker-compose -p app-php -f docker-compose.yml up -d --build
