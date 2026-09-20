// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using Authorization.Api.Domain.Invitation;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Mapster;

namespace Authorization.Api.UseCase.Auth;

/// <summary>OAuth ログイン DTO です。</summary>
public sealed record LoginDto(
    int Provider,
    string ProviderId,
    string Name,
    string Email,
    string? Avatar,
    string? InvitationToken = null
);

/// <summary>認証Serviceクラスです。</summary>
/// <param name="staffRepo">スタッフリポジトリ</param>
/// <param name="invitationAuthRepo">招待認可キャッシュリポジトリ</param>
public sealed class AuthService(IStaffRepository staffRepo, IInvitationAuthRepository invitationAuthRepo)
{
    /// <summary>スタッフを ID で取得します。</summary>
    /// <param name="id">スタッフID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>スタッフ</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<Domain.Staff.Staff> FindUserAsync(long id, CancellationToken ct = default) =>
        await staffRepo.FindByIdAsync(id, ct) ?? throw AppException.NotFound("staff_not_found");

    /// <summary>
    /// OAuth プロバイダー情報でログインします。既存スタッフは最終ログイン日時等を更新し、
    /// 未登録の場合は招待トークンの認可キャッシュを検証してスタッフを新規登録します。
    /// </summary>
    /// <param name="dto">OAuthプロバイダー情報・招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>ログイン（または新規登録）したスタッフ</returns>
    /// <exception cref="AppException">招待が無い場合（403 invitation_required）</exception>
    public async Task<Domain.Staff.Staff> LoginAsync(LoginDto dto, CancellationToken ct = default)
    {
        var now      = DateTime.Now;
        var existing = await staffRepo.FindByProviderAsync(dto.Provider, dto.ProviderId, ct);

        Domain.Staff.Staff staff;
        if (existing is not null)
        {
            staff = existing with { Avatar = dto.Avatar, LastLoginAt = now, UpdatedAt = now };
        }
        else
        {
            var token = dto.InvitationToken;
            var role  = string.IsNullOrEmpty(token) ? null : await invitationAuthRepo.GetRoleAsync(token, ct);
            if (role is null) throw AppException.Forbidden("invitation_required");

            // dto.Adapt<Staff>() が Name/Email/Provider/ProviderId/Avatar をMapsterで
            // 自動マッピングする。招待ロールから決まるRole等はwithで上書きする。
            staff = dto.Adapt<Domain.Staff.Staff>() with
            {
                Role        = StaffRole.From(role.Value),
                LastLoginAt = now,
                CreatedAt   = now,
                UpdatedAt   = now,
            };

            var saved = await staffRepo.SaveAsync(staff, ct);
            // DB保存が成功した後に招待トークンを消費する。逆順だとDB保存失敗時に
            // トークンだけ失われ、招待された本人が再ログインできなくなる。
            await invitationAuthRepo.RemoveAsync(token!, ct);
            return saved;
        }

        return await staffRepo.SaveAsync(staff, ct);
    }
}
