/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.config;

/**
 * アプリケーション設定です。
 *
 * @param app アプリケーション設定
 * @param db データベース設定
 * @param redis Redis設定
 * @param oauth OAuth設定
 * @param jwt JWT設定
 * @param mail メール設定
 * @param aws AWS設定
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public record AppConfig(
        App app, Db db, Redis redis, OAuth oauth, Jwt jwt, Mail mail, Aws aws) {

    /**
     * アプリケーション設定です。
     *
     * @param env 実行環境（local/testing/develop/staging/production）
     * @param port アプリケーションのリスンポート
     * @param frontendUrl フロントエンドURL
     * @param staffCookieLifetime staff_id クッキーの有効期限（秒）
     * @param notificationDefaultLimit 通知一覧のデフォルト取得件数
     * @param cachePrefix キャッシュキーのプレフィックス
     * @param runtime ランタイム識別子（OAuthのstateに埋め込む）
     */
    public record App(
            String env,
            int port,
            String frontendUrl,
            long staffCookieLifetime,
            long notificationDefaultLimit,
            String cachePrefix,
            String runtime) {
    }

    /**
     * データベース設定です。
     *
     * @param host 接続先ホスト
     * @param port 接続先ポート
     * @param database データベース名
     * @param username 接続ユーザー名
     * @param password 接続パスワード
     */
    public record Db(String host, int port, String database, String username, String password) {
    }

    /**
     * Redis設定です。
     *
     * @param host 接続先ホスト
     * @param port 接続先ポート
     * @param password 接続パスワード
     * @param db 使用するデータベース番号
     */
    public record Redis(String host, int port, String password, int db) {
    }

    /**
     * OAuth設定です。
     *
     * @param googleClientId GoogleのクライアントID
     * @param googleClientSecret Googleのクライアントシークレット
     * @param googleRedirectUrl GoogleのリダイレクトURL
     * @param githubClientId GitHubのクライアントID
     * @param githubClientSecret GitHubのクライアントシークレット
     * @param githubRedirectUrl GitHubのリダイレクトURL
     */
    public record OAuth(
            String googleClientId,
            String googleClientSecret,
            String googleRedirectUrl,
            String githubClientId,
            String githubClientSecret,
            String githubRedirectUrl) {
    }

    /**
     * JWT設定です。
     *
     * @param issuer 発行者（iss）
     * @param algorithm 署名アルゴリズム
     * @param ttl JWTの有効期限（秒）
     * @param cacheTtl JWTキャッシュの有効期限（秒）
     */
    public record Jwt(String issuer, String algorithm, long ttl, long cacheTtl) {
    }

    /**
     * メール設定です。
     *
     * @param host SMTPホスト
     * @param port SMTPポート
     * @param username 認証ユーザー名
     * @param password 認証パスワード
     * @param fromAddress 送信元アドレス
     * @param appName メール件名に使うアプリ名
     * @param appEnv 実行環境（件名プレフィックスの判定に使用）
     */
    public record Mail(
            String host, String port, String username, String password, String fromAddress, String appName,
            String appEnv) {
    }

    /**
     * AWS設定です。
     *
     * @param region リージョン
     * @param endpoint エンドポイント（LocalStack等への上書き用、未指定時はnull）
     * @param accessKey アクセスキー
     * @param secretKey シークレットキー
     */
    public record Aws(String region, String endpoint, String accessKey, String secretKey) {
    }
}
