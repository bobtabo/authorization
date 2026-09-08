/*
 * アプリケーション設定モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Config;

/// <summary>アプリ全般の設定です。</summary>
public sealed record AppSettings(
    string Name,
    string Env,
    int Port,
    string Timezone,
    string FrontendUrl,
    long StaffCookieLifetime,
    int NotificationDefaultLimit,
    string CachePrefix,
    string Runtime
);

/// <summary>DB 接続設定です。</summary>
public sealed record DbSettings(string Host, int Port, string Database, string User, string Password)
{
    /// <summary>MySqlConnector 用の接続文字列を返します。</summary>
    /// <returns>接続文字列</returns>
    public string ConnectionString =>
        $"Server={Host};Port={Port};Database={Database};User={User};Password={Password};" +
        "SslMode=None;AllowPublicKeyRetrieval=true;CharSet=utf8mb4;";
}

/// <summary>Redis 接続設定です。</summary>
public sealed record RedisSettings(string Host, int Port, string Password, int Database);

/// <summary>OAuth2 クライアント設定です。</summary>
public sealed record OAuthSettings(
    string GoogleClientId,
    string GoogleClientSecret,
    string GoogleRedirectUrl,
    string GithubClientId,
    string GithubClientSecret,
    string GithubRedirectUrl
);

/// <summary>Gate JWT 設定です。</summary>
public sealed record JwtSettings(string Issuer, string Algorithm, long Ttl, long CacheTtl);

/// <summary>メール設定です。</summary>
public sealed record MailSettings(string AppName, string AppEnv, string FromAddress);

/// <summary>AWS（SES）設定です。</summary>
public sealed record AwsSettings(string Region, string Endpoint, string AccessKey, string SecretKey);

/// <summary>アプリケーション全体の設定です。</summary>
public sealed record AppConfig(
    AppSettings App,
    DbSettings Db,
    RedisSettings Redis,
    OAuthSettings OAuth,
    JwtSettings Jwt,
    MailSettings Mail,
    AwsSettings Aws
);
