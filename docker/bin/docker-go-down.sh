#!/bin/bash
#
# Go コンテナ環境を停止／破棄
#

cd local/app-go
docker-compose -p app-go -f docker-compose.yml down --rmi all --volumes