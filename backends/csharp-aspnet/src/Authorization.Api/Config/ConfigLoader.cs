/*
 * 設定読み込みモジュール。
 *
 * .env ファイルと環境変数から AppConfig を組み立てます。環境変数が優先されます。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Config;

/// <summary>.env と環境変数から設定を読み込みます。</summary>
public static class ConfigLoader
{
    /// <summary>
    /// 設定を読み込みます。
    /// </summary>
    /// <param name="baseDir">.env を探索するディレクトリ（省略時はカレントディレクトリ）</param>
    /// <param name="envFile">読み込む .env ファイル名（省略時は APP_ENV に応じて自動判定）</param>
    public static AppConfig Load(string? baseDir = null, string? envFile = null)
    {
        var file = envFile ?? ResolveEnvFile();
        var dir  = baseDir ?? FindEnvDir(Directory.GetCurrentDirectory(), file);
        var vars = new Dictionary<string, string>(StringComparer.Ordinal);

        foreach (var name in new[] { file, $"{file}.local" })
        {
            var path = Path.Combine(dir, name);
            if (File.Exists(path))
            {
                foreach (var (k, v) in DotEnv.Parse(File.ReadAllLines(path))) vars[k] = v;
            }
        }

        string Str(string key, string def = "")
        {
            var env = Environment.GetEnvironmentVariable(key);
            if (!string.IsNullOrEmpty(env)) return env;
            return vars.TryGetValue(key, out var v) && v.Length > 0 ? v : def;
        }
        int  Int(string key, int def)   => int.TryParse(Str(key), out var v) ? v : def;
        long Long(string key, long def) => long.TryParse(Str(key), out var v) ? v : def;

        var appName = Str("APP_NAME", "Authorization Gateway");
        var appEnv  = Str("APP_ENV", "local");

        return new AppConfig(
            App: new AppSettings(
                Name:                     appName,
                Env:                      appEnv,
                Port:                     Int("APP_PORT", 8080),
                Timezone:                 Str("APP_TIMEZONE", "Asia/Tokyo"),
                FrontendUrl:              Str("FRONTEND_URL", "http://localhost:3000"),
                StaffCookieLifetime:      Long("STAFF_COOKIE_LIFETIME", 60),
                NotificationDefaultLimit: Int("NOTIFICATION_DEFAULT_LIMIT", 10),
                CachePrefix:              Str("CACHE_PREFIX", "authorization-gateway"),
                Runtime:                  Str("APP_RUNTIME", "csharp")
            ),
            Db: new DbSettings(
                Host:     Str("DB_HOST", "127.0.0.1"),
                Port:     Int("DB_PORT", 3306),
                Database: Str("DB_DATABASE", "authorization"),
                User:     Str("DB_USERNAME", "develop"),
                Password: Str("DB_PASSWORD", "")
            ),
            Redis: new RedisSettings(
                Host:     Str("REDIS_HOST", "127.0.0.1"),
                Port:     Int("REDIS_PORT", 6379),
                Password: Str("REDIS_PASSWORD", ""),
                Database: Int("REDIS_DB", 0)
            ),
            OAuth: new OAuthSettings(
                GoogleClientId:     Str("GOOGLE_CLIENT_ID"),
                GoogleClientSecret: Str("GOOGLE_CLIENT_SECRET"),
                GoogleRedirectUrl:  Str("GOOGLE_REDIRECT_URL"),
                GithubClientId:     Str("GITHUB_CLIENT_ID"),
                GithubClientSecret: Str("GITHUB_CLIENT_SECRET"),
                GithubRedirectUrl:  Str("GITHUB_REDIRECT_URL")
            ),
            Jwt: new JwtSettings(
                Issuer:    "authorization",
                Algorithm: "RS256",
                Ttl:       1800,
                CacheTtl:  Long("GATE_JWT_CACHE_TTL", 1800)
            ),
            Mail: new MailSettings(
                AppName:     appName,
                AppEnv:      appEnv,
                FromAddress: Str("MAIL_FROM_ADDRESS", "no-reply@example.com")
            ),
            Aws: new AwsSettings(
                Region:    Str("AWS_REGION", "ap-northeast-1"),
                Endpoint:  Str("AWS_ENDPOINT_URL"),
                AccessKey: Str("AWS_ACCESS_KEY_ID"),
                SecretKey: Str("AWS_SECRET_ACCESS_KEY")
            )
        );
    }

    private static string ResolveEnvFile()
    {
        var env = Environment.GetEnvironmentVariable("APP_ENV");
        return env == "testing" ? ".env.testing" : ".env";
    }

    /// <summary>
    /// start から親方向へ辿り、.env（または .env.local）が存在する最初のディレクトリを返します。
    /// 見つからない場合は start を返します。
    /// </summary>
    private static string FindEnvDir(string start, string file)
    {
        for (var dir = new DirectoryInfo(start); dir is not null; dir = dir.Parent)
        {
            if (File.Exists(Path.Combine(dir.FullName, file)) || File.Exists(Path.Combine(dir.FullName, $"{file}.local")))
            {
                return dir.FullName;
            }
        }
        return start;
    }
}

/// <summary>.env 形式の簡易パーサーです。</summary>
public static class DotEnv
{
    /// <summary>
    /// KEY=VALUE 形式の行を解析します。# から始まる行は無視し、引用符は取り除きます。
    /// </summary>
    public static IEnumerable<KeyValuePair<string, string>> Parse(IEnumerable<string> lines)
    {
        foreach (var raw in lines)
        {
            var line = raw.Trim();
            if (line.Length == 0 || line.StartsWith('#')) continue;
            if (line.StartsWith("export ", StringComparison.Ordinal)) line = line[7..].TrimStart();

            var eq = line.IndexOf('=');
            if (eq <= 0) continue;

            var key   = line[..eq].Trim();
            var value = line[(eq + 1)..].Trim();

            if (value.Length >= 2 && (value[0] == '"' || value[0] == '\'') && value[^1] == value[0])
            {
                value = value[1..^1];
            }
            else
            {
                var hash = value.IndexOf(" #", StringComparison.Ordinal);
                if (hash >= 0) value = value[..hash].TrimEnd();
            }

            yield return new KeyValuePair<string, string>(key, value);
        }
    }
}
