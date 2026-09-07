/*
 * Gate（認可）ドメインモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Domain.Gate;

/// <summary>JWT 発行結果 VO です。</summary>
public sealed record GateIssueVo(string Token);

/// <summary>JWT 検証結果 VO です（クレームをそのまま保持）。</summary>
public sealed record GateVerifyVo(IReadOnlyDictionary<string, object?> Claims);

/// <summary>発行済み JWT のキャッシュリポジトリです。</summary>
public interface IGateCacheRepository
{
    Task<string?> GetJwtAsync(string identifier, string memberId, CancellationToken ct = default);
    Task PutJwtAsync(string identifier, string memberId, string token, long ttlSeconds, CancellationToken ct = default);
}
