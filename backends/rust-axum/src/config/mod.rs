//! アプリケーション設定モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::env;

#[derive(Clone)]
pub struct Config {
    pub app:   AppConfig,
    pub db:    DbConfig,
    pub redis: RedisConfig,
    pub oauth: OAuthConfig,
    pub jwt:   JwtConfig,
    pub mail:  MailConfig,
}

#[derive(Clone)]
pub struct AppConfig {
    pub env:                        String,
    pub port:                       String,
    pub frontend_url:               String,
    pub staff_cookie_lifetime:      i64,
    pub notification_default_limit: i64,
    pub cache_prefix:               String,
}

#[derive(Clone)]
pub struct DbConfig {
    pub dsn: String,
}

#[derive(Clone)]
pub struct RedisConfig {
    pub addr:     String,
    pub password: String,
    pub db:       i64,
}

#[derive(Clone)]
pub struct OAuthConfig {
    pub google_client_id:     String,
    pub google_client_secret: String,
    pub google_redirect_url:  String,
}

#[derive(Clone)]
pub struct JwtConfig {
    pub issuer:    String,
    pub algorithm: String,
    pub ttl:       i64,
    pub cache_ttl: i64,
}

#[derive(Clone)]
pub struct MailConfig {
    pub host:         String,
    pub port:         String,
    pub username:     String,
    pub password:     String,
    pub from_address: String,
    pub app_name:     String,
    pub app_env:      String,
}

impl Config {
    pub fn load() -> Self {
        // ENV_FILE 指定があればそれを使う。なければ .env.testing.local → .env.testing → .env の順で先読み。
        // dotenvy::from_filename は既存の env var を上書きしないため CI のシステム env vars が優先される。
        if let Ok(f) = env::var("ENV_FILE") {
            let _ = dotenvy::from_filename(&f);
        } else {
            let _ = dotenvy::from_filename(".env.testing.local");
            let _ = dotenvy::from_filename(".env.testing");
            let _ = dotenvy::from_filename(".env");
        }

        Config {
            app: AppConfig {
                env:                        get_env("APP_ENV", "local"),
                port:                       get_env("APP_PORT", "8080"),
                frontend_url:               get_env("FRONTEND_URL", "http://localhost:3000"),
                staff_cookie_lifetime:      get_env_i64("STAFF_COOKIE_LIFETIME", 60),
                notification_default_limit: get_env_i64("NOTIFICATION_DEFAULT_LIMIT", 10),
                cache_prefix:               get_env("CACHE_PREFIX", ""),
            },
            db: DbConfig {
                dsn: build_dsn(),
            },
            redis: RedisConfig {
                addr:     format!("{}:{}", get_env("REDIS_HOST", "localhost"), get_env("REDIS_PORT", "6379")),
                password: get_env("REDIS_PASSWORD", ""),
                db:       get_env_i64("REDIS_DB", 0),
            },
            oauth: OAuthConfig {
                google_client_id:     get_env("GOOGLE_CLIENT_ID", ""),
                google_client_secret: get_env("GOOGLE_CLIENT_SECRET", ""),
                google_redirect_url:  get_env("GOOGLE_REDIRECT_URL", ""),
            },
            jwt: JwtConfig {
                issuer:    "authorization".to_string(),
                algorithm: "RS256".to_string(),
                ttl:       1800,
                cache_ttl: get_env_i64("GATE_JWT_CACHE_TTL", 1800),
            },
            mail: MailConfig {
                host:         get_env("MAIL_HOST", "localhost"),
                port:         get_env("MAIL_PORT", "1025"),
                username:     get_env("MAIL_USERNAME", ""),
                password:     get_env("MAIL_PASSWORD", ""),
                from_address: get_env("MAIL_FROM_ADDRESS", "no-reply@example.com"),
                app_name:     get_env("APP_NAME", "Authorization Gateway"),
                app_env:      get_env("APP_ENV", "local"),
            },
        }
    }
}

fn build_dsn() -> String {
    let host = get_env("DB_HOST", "localhost");
    let port = get_env("DB_PORT", "3306");
    let user = get_env("DB_USERNAME", "root");
    let pass = url_encode_password(&get_env("DB_PASSWORD", ""));
    let name = get_env("DB_DATABASE", "authorization");
    format!("mysql://{}:{}@{}:{}/{}?ssl-mode=disabled", user, pass, host, port, name)
}

fn url_encode_password(s: &str) -> String {
    s.chars().flat_map(|c| match c {
        '#' => "%23".chars().collect::<Vec<_>>(),
        '@' => "%40".chars().collect::<Vec<_>>(),
        ':' => "%3A".chars().collect::<Vec<_>>(),
        '/' => "%2F".chars().collect::<Vec<_>>(),
        '?' => "%3F".chars().collect::<Vec<_>>(),
        '%' => "%25".chars().collect::<Vec<_>>(),
        c   => vec![c],
    }).collect()
}

fn get_env(key: &str, fallback: &str) -> String {
    env::var(key).unwrap_or_else(|_| fallback.to_string())
}

fn get_env_i64(key: &str, fallback: i64) -> i64 {
    env::var(key).ok().and_then(|v| v.parse().ok()).unwrap_or(fallback)
}
