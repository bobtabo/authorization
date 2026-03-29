#!/bin/bash
#
# Python コンテナ環境を停止／破棄
#

cd local/app-python
docker-compose -p app-python -f docker-compose.yml down --rmi all --volumes