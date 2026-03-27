#!/bin/bash
#
# コンテナ設定
#

cd ./local/backends/common
cp ./environment/default.crt ./proxy/certs/default.crt
cp ./environment/default.key ./proxy/certs/default.key
cp ./environment/.env.example ./.env