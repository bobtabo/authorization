/*
 * 招待ユースケースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Invitation;
using Authorization.Api.Support;

namespace Authorization.Api.UseCase.Invitation;

/// <summary>招待ユースケースです。</summary>
public sealed class InvitationInteractor(IInvitationRepository invitationRepo, IInvitationAuthRepository invitationAuthRepo)
{
    private const long AuthTtlSeconds = 600;

    /// <summary>指定ロールの現在の招待を返します。</summary>
    /// <param name="role">ロール種別（1=管理者、2=メンバー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>現在有効な招待</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<InvitationVo> CurrentAsync(int role, CancellationToken ct = default) =>
        await invitationRepo.GetCurrentByRoleAsync(role, ct) ?? throw AppException.NotFound("invitation_not_found");

    /// <summary>指定ロールの招待を新規発行します。</summary>
    /// <param name="role">ロール種別（1=管理者、2=メンバー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>新規発行した招待</returns>
    public Task<InvitationVo> IssueAsync(int role, CancellationToken ct = default) =>
        invitationRepo.IssueAsync(role, ct);

    /// <summary>
    /// 招待トークンを検索し、見つかった場合はログイン時に参照する認可キャッシュへ保存します。
    /// </summary>
    /// <param name="token">招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>招待</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<InvitationVo> FindByTokenAsync(string token, CancellationToken ct = default)
    {
        var vo = await invitationRepo.FindByTokenAsync(token, ct) ?? throw AppException.NotFound("invitation_not_found");
        await invitationAuthRepo.PutRoleAsync(vo.Token, vo.Role, AuthTtlSeconds, ct);
        return vo;
    }
}
