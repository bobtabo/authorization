#!/bin/bash
#
# TypeScript コンテナ環境に接続
#

cd local/app-ts
docker-compose -p app-ts exec --user 1000 node sh