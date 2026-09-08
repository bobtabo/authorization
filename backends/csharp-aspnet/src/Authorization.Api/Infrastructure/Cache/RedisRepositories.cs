/*
 * Redis キャッシュリポジトリモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Config;
using Authorization.Api.Domain.Gate;
using Authorization.Api.Domain.Invitation;
using StackExchange.Redis;

namespace Authorization.Api.Infrastructure.Cache;

/// <summary>Redis 接続を生成します。</summary>
public static class RedisConnection
{
    /// <summary>設定内容でRedis接続を確立します。</summary>
    /// <param name="cfg">Redis接続設定</param>
    /// <returns>接続済みのマルチプレクサー</returns>
    public static ConnectionMultiplexer Connect(RedisSettings cfg)
    {
        var options = new ConfigurationOptions
        {
            EndPoints          = { { cfg.Host, cfg.Port } },
            DefaultDatabase    = cfg.Database,
            ConnectTimeout     = 2000,
            AbortOnConnectFail = false,
        };
        if (!string.IsNullOrWhiteSpace(cfg.Password)) options.Password = cfg.Password;
        return ConnectionMultiplexer.Connect(options);
    }
}

/// <summary>Redis による Gate JWT キャッシュです。</summary>
public sealed class RedisGateRepository(IConnectionMultiplexer redis, AppSettings app) : IGateCacheRepository
{
    /// <summary>Redisキーを組み立てます。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="memberId">メンバーID</param>
    /// <returns>Redisキー</returns>
    private string Key(string identifier, string memberId) => $"{app.CachePrefix}:gate.jwt:{identifier}:{memberId}";

    /// <inheritdoc/>
    public async Task<string?> GetJwtAsync(string identifier, string memberId, CancellationToken ct = default)
    {
        var v = await redis.GetDatabase().StringGetAsync(Key(identifier, memberId));
        return v.IsNullOrEmpty ? null : v.ToString();
    }

    /// <inheritdoc/>
    public Task PutJwtAsync(string identifier, string memberId, string token, long ttlSeconds, CancellationToken ct = default) =>
        redis.GetDatabase().StringSetAsync(Key(identifier, memberId), token, TimeSpan.FromSeconds(ttlSeconds));
}

/// <summary>Redis による招待認可キャッシュです。</summary>
public sealed class RedisInvitationAuthRepository(IConnectionMultiplexer redis, AppSettings app) : IInvitationAuthRepository
{
    /// <summary>Redisキーを組み立てます。</summary>
    /// <param name="token">招待トークン</param>
    /// <returns>Redisキー</returns>
    private string Key(string token) => $"{app.CachePrefix}:invitation_auth:invitation_auth:{token}";

    /// <inheritdoc/>
    public Task PutRoleAsync(string token, int role, long ttlSeconds, CancellationToken ct = default) =>
        redis.GetDatabase().StringSetAsync(Key(token), role.ToString(), TimeSpan.FromSeconds(ttlSeconds));

    /// <inheritdoc/>
    public async Task<int?> GetRoleAsync(string token, CancellationToken ct = default)
    {
        var v = await redis.GetDatabase().StringGetAsync(Key(token));
        return v.IsNullOrEmpty ? null : int.TryParse(v.ToString(), out var role) ? role : null;
    }

    /// <inheritdoc/>
    public Task RemoveAsync(string token, CancellationToken ct = default) =>
        redis.GetDatabase().KeyDeleteAsync(Key(token));
}
