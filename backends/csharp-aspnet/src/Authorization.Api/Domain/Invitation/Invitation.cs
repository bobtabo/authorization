// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
namespace Authorization.Api.Domain.Invitation;

/// <summary>招待 VO です。</summary>
public sealed record InvitationVo(string Token, int Role, string Url, string DisplayUrl);

/// <summary>招待リポジトリです。</summary>
public interface IInvitationRepository
{
    /// <summary>指定ロールの現在有効な招待を取得します。</summary>
    /// <param name="role">ロール種別（1=管理者、2=メンバー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>招待、存在しない場合はnull</returns>
    Task<InvitationVo?> GetCurrentByRoleAsync(int role, CancellationToken ct = default);

    /// <summary>指定ロールで招待トークンを新規発行します。</summary>
    /// <param name="role">ロール種別（1=管理者、2=メンバー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>新規発行した招待</returns>
    Task<InvitationVo> IssueAsync(int role, CancellationToken ct = default);

    /// <summary>招待トークンに一致する招待を取得します。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>招待、存在しない場合はnull</returns>
    Task<InvitationVo?> FindByTokenAsync(string token, CancellationToken ct = default);

    /// <summary>
    /// 招待トークンを論理削除します（ローテーション時に古いトークンを無効化し、
    /// 以後 <see cref="FindByTokenAsync"/> がキャッシュを再生成できないようにするため）。
    /// </summary>
    /// <param name="token">招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    Task RetireAsync(string token, CancellationToken ct = default);
}

/// <summary>招待トークン→ロールの認可キャッシュです（Redis 等）。</summary>
public interface IInvitationAuthRepository
{
    /// <summary>招待トークンに対応するロールを保存します。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="role">ロール種別（1=管理者、2=メンバー）</param>
    /// <param name="ttlSeconds">有効期間（秒）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    Task PutRoleAsync(string token, int role, long ttlSeconds, CancellationToken ct = default);

    /// <summary>招待トークンに対応するロールを取得します。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>ロール種別、無ければnull</returns>
    Task<int?> GetRoleAsync(string token, CancellationToken ct = default);

    /// <summary>招待トークンのキャッシュを削除します。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    Task RemoveAsync(string token, CancellationToken ct = default);
}
