/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.config;

/**
 * アプリケーション設定です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public record AppConfig(
        App app, Db db, Redis redis, OAuth oauth, Jwt jwt, Mail mail, Aws aws) {

    public record App(
            String env,
            int port,
            String frontendUrl,
            long staffCookieLifetime,
            long notificationDefaultLimit,
            String cachePrefix,
            String runtime) {
    }

    public record Db(String host, int port, String database, String username, String password) {
    }

    public record Redis(String host, int port, String password, int db) {
    }

    public record OAuth(
            String googleClientId,
            String googleClientSecret,
            String googleRedirectUrl,
            String githubClientId,
            String githubClientSecret,
            String githubRedirectUrl) {
    }

    public record Jwt(String issuer, String algorithm, long ttl, long cacheTtl) {
    }

    public record Mail(
            String host, String port, String username, String password, String fromAddress, String appName,
            String appEnv) {
    }

    public record Aws(String region, String endpoint, String accessKey, String secretKey) {
    }
}
