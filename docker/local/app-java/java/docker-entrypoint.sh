#!/bin/bash
set -e

# 名前付きボリュームはbindマウント配下にネストされているため root:root 0755 で
# 新規作成される。DOCKER_USER が書き込めるよう、root として起動した時点で
# 所有者を付け替えてから DOCKER_USER に降格する。
# APP_NAME・DOCKER_USER はcompose/Dockerfile側の実際の値と揃える（ハードコードすると
# .env で上書きされた場合にマウントパス・所有者がずれてchownが空振りする）。
APP_NAME="${APP_NAME:-auth-java}"
DOCKER_USER="${DOCKER_USER:-docker}"

for dir in build .gradle; do
    mkdir -p "/var/www/${APP_NAME}/$dir"
    chown "${DOCKER_USER}:${DOCKER_USER}" "/var/www/${APP_NAME}/$dir"
done

exec setpriv --reuid="${DOCKER_USER}" --regid="${DOCKER_USER}" --init-groups "$@"
