#!/bin/bash
#
# いろいろクリア（ローカル用）
#

php artisan view:clear
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan clear-compiled
php artisan config:cache
composer dump-autoload
find . -name '.DS_Store' -type f -ls -delete
