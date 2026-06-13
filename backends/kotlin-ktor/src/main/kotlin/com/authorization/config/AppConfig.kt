package com.authorization.config

import io.github.cdimascio.dotenv.dotenv

data class AppConfig(
    val env: String,
    val port: Int,
    val frontendUrl: String,
    val staffCookieLifetime: Long,
    val notificationDefaultLimit: Long,
    val cachePrefix: String,
    val runtime: String,
)

data class DbConfig(
    val dsn: String,
    val username: String,
    val password: String,
)

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String,
    val db: Int,
)

data class OAuthConfig(
    val googleClientId: String,
    val googleClientSecret: String,
    val googleRedirectUrl: String,
    val githubClientId: String,
    val githubClientSecret: String,
    val githubRedirectUrl: String,
)

data class JwtConfig(
    val issuer: String,
    val algorithm: String,
    val ttl: Long,
    val cacheTtl: Long,
)

data class MailConfig(
    val host: String,
    val port: String,
    val username: String,
    val password: String,
    val fromAddress: String,
    val appName: String,
    val appEnv: String,
)

data class AwsConfig(
    val region: String,
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
)

data class Config(
    val app: AppConfig,
    val db: DbConfig,
    val redis: RedisConfig,
    val oauth: OAuthConfig,
    val jwt: JwtConfig,
    val mail: MailConfig,
    val aws: AwsConfig,
)

object ConfigLoader {
    fun load(): Config {
        val envFile = System.getenv("ENV_FILE") ?: ".env"
        val env = dotenv {
            filename = envFile
            ignoreIfMissing = true
        }

        fun str(key: String, default: String = "") = env.get(key, default)
        fun int(key: String, default: Int) = env.get(key, default.toString()).toIntOrNull() ?: default
        fun long(key: String, default: Long) = env.get(key, default.toString()).toLongOrNull() ?: default

        val dbHost = str("DB_HOST", "localhost")
        val dbPort = str("DB_PORT", "3306")
        val dbUser = str("DB_USERNAME", "root")
        val dbPass = str("DB_PASSWORD", "")
        val dbName = str("DB_DATABASE", "authorization")
        val dsn = "jdbc:mysql://$dbHost:$dbPort/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo"

        return Config(
            app = AppConfig(
                env                      = str("APP_ENV", "local"),
                port                     = int("APP_PORT", 8080),
                frontendUrl              = str("FRONTEND_URL", "http://localhost:3000"),
                staffCookieLifetime      = long("STAFF_COOKIE_LIFETIME", 60),
                notificationDefaultLimit = long("NOTIFICATION_DEFAULT_LIMIT", 10),
                cachePrefix              = str("CACHE_PREFIX", ""),
                runtime                  = str("APP_RUNTIME", "kotlin"),
            ),
            db = DbConfig(
                dsn      = dsn,
                username = dbUser,
                password = dbPass,
            ),
            redis = RedisConfig(
                host     = str("REDIS_HOST", "localhost"),
                port     = int("REDIS_PORT", 6379),
                password = str("REDIS_PASSWORD", ""),
                db       = int("REDIS_DB", 0),
            ),
            oauth = OAuthConfig(
                googleClientId     = str("GOOGLE_CLIENT_ID", ""),
                googleClientSecret = str("GOOGLE_CLIENT_SECRET", ""),
                googleRedirectUrl  = str("GOOGLE_REDIRECT_URL", ""),
                githubClientId     = str("GITHUB_CLIENT_ID", ""),
                githubClientSecret = str("GITHUB_CLIENT_SECRET", ""),
                githubRedirectUrl  = str("GITHUB_REDIRECT_URL", ""),
            ),
            jwt = JwtConfig(
                issuer    = "authorization",
                algorithm = "RS256",
                ttl       = 1800,
                cacheTtl  = long("GATE_JWT_CACHE_TTL", 1800),
            ),
            mail = MailConfig(
                host        = str("MAIL_HOST", "localhost"),
                port        = str("MAIL_PORT", "1025"),
                username    = str("MAIL_USERNAME", ""),
                password    = str("MAIL_PASSWORD", ""),
                fromAddress = str("MAIL_FROM_ADDRESS", "no-reply@example.com"),
                appName     = str("APP_NAME", "Authorization Gateway"),
                appEnv      = str("APP_ENV", "local"),
            ),
            aws = AwsConfig(
                region    = str("AWS_REGION", "ap-northeast-1"),
                endpoint  = str("AWS_ENDPOINT_URL", ""),
                accessKey = str("AWS_ACCESS_KEY_ID", ""),
                secretKey = str("AWS_SECRET_ACCESS_KEY", ""),
            ),
        )
    }
}
