/*
 * 招待 ドメインモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Domain.Invitation;

/// <summary>招待 VO です。</summary>
public sealed record InvitationVo(string Token, int Role, string Url, string DisplayUrl);

/// <summary>招待リポジトリです。</summary>
public interface IInvitationRepository
{
    /// <summary>指定ロールの現在有効な招待を取得します。</summary>
    Task<InvitationVo?> GetCurrentByRoleAsync(int role, CancellationToken ct = default);

    /// <summary>指定ロールで招待トークンを新規発行します。</summary>
    Task<InvitationVo> IssueAsync(int role, CancellationToken ct = default);

    /// <summary>招待トークンに一致する招待を取得します。</summary>
    Task<InvitationVo?> FindByTokenAsync(string token, CancellationToken ct = default);
}

/// <summary>招待トークン→ロールの認可キャッシュです（Redis 等）。</summary>
public interface IInvitationAuthRepository
{
    /// <summary>招待トークンに対応するロールを保存します。</summary>
    Task PutRoleAsync(string token, int role, long ttlSeconds, CancellationToken ct = default);

    /// <summary>招待トークンに対応するロールを取得します。</summary>
    Task<int?> GetRoleAsync(string token, CancellationToken ct = default);

    /// <summary>招待トークンのキャッシュを削除します。</summary>
    Task RemoveAsync(string token, CancellationToken ct = default);
}
