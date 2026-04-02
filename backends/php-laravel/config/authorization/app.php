<?php
/**
 * アプリ設定
 */

return [
    //キャッシュPrefix
    'cache_prefix' => env('CACHE_PREFIX'),
    //クエリーキャッシュ
    'query_cache' => env('DB_QUERY_CACHE', false),
    //メール設定
    'mail' => [
        'host' => env('MAIL_HOST'),
        'port' => env('MAIL_PORT'),
        'from' => env('MAIL_FROM_ADDRESS'),
        'subject' => [
            'prefix' => '【' . env('APP_NAME') . '】',
            'access_token' => 'アクセストークンのお知らせ',
        ],
        'template' => [
            'access_token' => 'templates.mails.access-token',
        ],
    ],
];
