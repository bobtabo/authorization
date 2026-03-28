#!/bin/bash
#
# PHP コンテナ環境に接続
#

cd local/app-php
docker-compose -p app-php exec --user 1000 php bash