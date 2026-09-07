/*
 * 認証ユースケースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Invitation;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;

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

/// <summary>認証ユースケースです。</summary>
public sealed class AuthInteractor(IStaffRepository staffRepo, IInvitationAuthRepository invitationAuthRepo)
{
    /// <summary>スタッフを ID で取得します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<Domain.Staff.Staff> FindUserAsync(long id, CancellationToken ct = default) =>
        await staffRepo.FindByIdAsync(id, ct) ?? throw AppException.NotFound("staff_not_found");

    /// <summary>
    /// OAuth プロバイダー情報でログインします。既存スタッフは最終ログイン日時等を更新し、
    /// 未登録の場合は招待トークンの認可キャッシュを検証してスタッフを新規登録します。
    /// </summary>
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

            await invitationAuthRepo.RemoveAsync(token!, ct);
            staff = new Domain.Staff.Staff
            {
                Name        = dto.Name,
                Email       = dto.Email,
                Provider    = dto.Provider,
                ProviderId  = dto.ProviderId,
                Avatar      = dto.Avatar,
                Role        = StaffRole.From(role.Value),
                LastLoginAt = now,
                CreatedAt   = now,
                UpdatedAt   = now,
            };
        }

        return await staffRepo.SaveAsync(staff, ct);
    }
}
