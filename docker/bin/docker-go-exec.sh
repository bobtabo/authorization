#!/bin/bash
#
# Go コンテナ環境に接続
#

cd local/app-go
docker-compose -p app-go exec --user 1000 go sh