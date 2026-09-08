/*
 * スタッフユースケースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;

namespace Authorization.Api.UseCase.Staff;

/// <summary>権限更新 DTO です。</summary>
public sealed record StaffUpdateRoleDto(long Id, int Role, long ExecutorId);

/// <summary>削除 DTO です。</summary>
public sealed record StaffDestroyDto(long Id, long ExecutorId);

/// <summary>スタッフユースケースです。</summary>
public sealed class StaffInteractor(IStaffRepository repo)
{
    /// <summary>条件に一致するスタッフ一覧と総件数を返します。</summary>
    public async Task<(List<StaffListItem> Items, int Count)> FindByConditionWithCountAsync(
        StaffCondition cond, CancellationToken ct = default)
    {
        var count = await repo.CountByConditionAsync(cond, ct);
        var items = (await repo.FindByConditionAsync(cond, ct))
            .Select(s => new StaffListItem(s.Id, s.Name, s.Email, s.Role, s.Status, s.CreatedAt, s.UpdatedAt, s.Version))
            .ToList();
        return (items, count);
    }

    /// <summary>権限を更新します。</summary>
    /// <exception cref="AppException">ロール値が不正な場合（400）、存在しない場合（404）</exception>
    public async Task UpdateRoleAsync(StaffUpdateRoleDto dto, CancellationToken ct = default)
    {
        if (!StaffRole.IsValid(dto.Role)) throw AppException.BadRequest("role_invalid");
        _ = await repo.FindByIdAsync(dto.Id, ct) ?? throw AppException.NotFound("staff_not_found");
        if (!await repo.UpdateRoleAsync(dto.Id, dto.Role, dto.ExecutorId, ct))
            throw AppException.NotFound("staff_not_found");
    }

    /// <summary>論理削除を取り消します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task RestoreAsync(long id, CancellationToken ct = default)
    {
        if (!await repo.RestoreAsync(id, ct)) throw AppException.NotFound("staff_not_found");
    }

    /// <summary>論理削除します。</summary>
    /// <exception cref="AppException">存在しない場合（404）、バージョン不一致（409）</exception>
    public async Task DestroyAsync(StaffDestroyDto dto, CancellationToken ct = default)
    {
        var staff = await repo.FindByIdAsync(dto.Id, ct) ?? throw AppException.NotFound("staff_not_found");
        await repo.SoftDeleteAsync(dto.Id, dto.ExecutorId, staff.Version, ct);
    }
}
