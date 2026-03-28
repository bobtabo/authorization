#!/bin/bash
#
# コンテナ環境を停止／破棄
#

cd local/common
docker-compose down --rmi all --volumes
docker network rm authorization
rm -fdR data
rm -fdR logs
