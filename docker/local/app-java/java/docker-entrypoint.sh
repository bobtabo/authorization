#!/bin/bash
set -e

# 名前付きボリュームはbindマウント配下にネストされているため root:root 0755 で
# 新規作成される。docker ユーザーが書き込めるよう、root として起動した時点で
# 所有者を付け替えてから docker ユーザーに降格する。
for dir in build .gradle; do
    mkdir -p "/var/www/auth-java/$dir"
    chown docker:docker "/var/www/auth-java/$dir"
done

exec setpriv --reuid=docker --regid=docker --init-groups "$@"
