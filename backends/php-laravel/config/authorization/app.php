<?php
/**
 * アプリ設定
 */

return [
    //キャッシュPrefix
    'cache_prefix' => env('CACHE_PREFIX'),
    //クエリーキャッシュ
    'query_cache' => env('DB_QUERY_CACHE', false),
];
