#!/bin/bash
#
# コンテナ環境を起動
#

docker network create --driver bridge authorization
docker-compose up -d --build
