<?php

/**
 * アプリ設定
 */

return [
    //フロントエンドURL
    'frontend_url' => env('FRONTEND_URL', 'http://localhost:5173'),
    //スタッフクッキー有効期間（分）
    'staff_cookie_lifetime' => (int)env('STAFF_COOKIE_LIFETIME', 60 * 24 * 7),
    //通知一覧デフォルト取得件数
    'notification_default_limit' => 10,
    //キャッシュPrefix
    'cache_prefix' => env('CACHE_PREFIX'),
    //クエリーキャッシュ
    'query_cache' => env('DB_QUERY_CACHE', false),
    //JWT キャッシュ有効期間（秒）。JWT の有効期限（30分）以下を推奨
    'jwt_cache_ttl' => (int) env('GATE_JWT_CACHE_TTL', 1800),
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
