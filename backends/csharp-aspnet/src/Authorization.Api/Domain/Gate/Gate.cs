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
    /// <summary>キャッシュ済みJWTを取得します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="memberId">メンバーID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>キャッシュ済みJWT、無ければnull</returns>
    Task<string?> GetJwtAsync(string identifier, string memberId, CancellationToken ct = default);

    /// <summary>JWTをキャッシュに保存します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="memberId">メンバーID</param>
    /// <param name="token">JWT文字列</param>
    /// <param name="ttlSeconds">有効期間（秒）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    Task PutJwtAsync(string identifier, string memberId, string token, long ttlSeconds, CancellationToken ct = default);
}
