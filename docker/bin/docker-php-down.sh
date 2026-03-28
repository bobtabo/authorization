#!/bin/bash
#
# PHP コンテナ環境を停止／破棄
#

cd local/app-php
docker-compose -p app-php -f docker-compose.yml down --rmi all --volumes