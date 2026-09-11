/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * `.env` から {@link AppConfig} を読み込む Spring 設定クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Configuration
public class ConfigLoader {

    @Bean
    public AppConfig appConfig() {
        String envFile = System.getenv("ENV_FILE") != null ? System.getenv("ENV_FILE") : ".env";
        Dotenv env = Dotenv.configure().filename(envFile).ignoreIfMissing().load();

        return new AppConfig(
                new AppConfig.App(
                        str(env, "APP_ENV", "local"),
                        intVal(env, "APP_PORT", 8080),
                        str(env, "FRONTEND_URL", "http://localhost:3000"),
                        longVal(env, "STAFF_COOKIE_LIFETIME", 60),
                        longVal(env, "NOTIFICATION_DEFAULT_LIMIT", 10),
                        str(env, "CACHE_PREFIX", ""),
                        str(env, "APP_RUNTIME", "java")),
                new AppConfig.Db(
                        str(env, "DB_HOST", "localhost"),
                        intVal(env, "DB_PORT", 3306),
                        str(env, "DB_DATABASE", "authorization"),
                        str(env, "DB_USERNAME", "root"),
                        str(env, "DB_PASSWORD", "")),
                new AppConfig.Redis(
                        str(env, "REDIS_HOST", "localhost"),
                        intVal(env, "REDIS_PORT", 6379),
                        str(env, "REDIS_PASSWORD", ""),
                        intVal(env, "REDIS_DB", 0)),
                new AppConfig.OAuth(
                        str(env, "GOOGLE_CLIENT_ID", ""),
                        str(env, "GOOGLE_CLIENT_SECRET", ""),
                        str(env, "GOOGLE_REDIRECT_URL", ""),
                        str(env, "GITHUB_CLIENT_ID", ""),
                        str(env, "GITHUB_CLIENT_SECRET", ""),
                        str(env, "GITHUB_REDIRECT_URL", "")),
                new AppConfig.Jwt("authorization", "RS256", 1800, longVal(env, "GATE_JWT_CACHE_TTL", 1800)),
                new AppConfig.Mail(
                        str(env, "MAIL_HOST", "localhost"),
                        str(env, "MAIL_PORT", "1025"),
                        str(env, "MAIL_USERNAME", ""),
                        str(env, "MAIL_PASSWORD", ""),
                        str(env, "MAIL_FROM_ADDRESS", "no-reply@example.com"),
                        str(env, "APP_NAME", "Authorization Gateway"),
                        str(env, "APP_ENV", "local")),
                new AppConfig.Aws(
                        str(env, "AWS_REGION", "ap-northeast-1"),
                        str(env, "AWS_ENDPOINT_URL", ""),
                        str(env, "AWS_ACCESS_KEY_ID", ""),
                        str(env, "AWS_SECRET_ACCESS_KEY", "")));
    }

    private static String str(Dotenv env, String key, String def) {
        String v = env.get(key);
        return v != null ? v : def;
    }

    private static int intVal(Dotenv env, String key, int def) {
        try {
            return Integer.parseInt(str(env, key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static long longVal(Dotenv env, String key, long def) {
        try {
            return Long.parseLong(str(env, key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
