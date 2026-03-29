#!/bin/bash
#
# TypeScript コンテナ環境を停止／破棄
#

cd local/app-ts
docker-compose -p app-ts -f docker-compose.yml down --rmi all --volumes