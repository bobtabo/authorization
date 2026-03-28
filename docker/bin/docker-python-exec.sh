#!/bin/bash
#
# Python コンテナ環境に接続
#

cd local/app-python
docker-compose -p app-python exec --user 1000 python bash