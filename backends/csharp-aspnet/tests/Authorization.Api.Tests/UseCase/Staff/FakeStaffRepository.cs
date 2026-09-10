using Authorization.Api.Domain.Staff;

namespace Authorization.Api.Tests.UseCase.Staff;

/// <summary>テスト用の手書きフェイクリポジトリです（モックライブラリは使わない）。</summary>
public sealed class FakeStaffRepository : IStaffRepository
{
    private readonly Dictionary<long, Authorization.Api.Domain.Staff.Staff> staffs = [];

    /// <summary>指定スタッフをリポジトリに追加します。</summary>
    public FakeStaffRepository Add(Authorization.Api.Domain.Staff.Staff s)
    {
        staffs[s.Id] = s;
        return this;
    }

    public int UpdateRoleCallCount { get; private set; }
    public int SoftDeleteCallCount { get; private set; }
    public int RestoreCallCount { get; private set; }
    public long? LastSoftDeletedId { get; private set; }
    public int? LastSoftDeletedVersion { get; private set; }

    public Task<int> CountByConditionAsync(StaffCondition cond, CancellationToken ct = default) =>
        Task.FromResult(staffs.Count);

    public Task<List<Authorization.Api.Domain.Staff.Staff>> FindByConditionAsync(StaffCondition cond, CancellationToken ct = default) =>
        Task.FromResult(staffs.Values.ToList());

    public Task<Authorization.Api.Domain.Staff.Staff?> FindByIdAsync(long id, CancellationToken ct = default) =>
        Task.FromResult(staffs.TryGetValue(id, out var s) && s.DeletedAt is null ? s : null);

    public Task<Authorization.Api.Domain.Staff.Staff?> FindByProviderAsync(int provider, string providerId, CancellationToken ct = default) =>
        Task.FromResult(staffs.Values.FirstOrDefault(s => s.Provider == provider && s.ProviderId == providerId));

    public Task<List<Authorization.Api.Domain.Staff.Staff>> FindAllActiveAsync(CancellationToken ct = default) =>
        Task.FromResult(staffs.Values.Where(s => s.DeletedAt is null).ToList());

    public Task<Authorization.Api.Domain.Staff.Staff> SaveAsync(Authorization.Api.Domain.Staff.Staff staff, CancellationToken ct = default)
    {
        var saved = staff.Id == 0 ? staff with { Id = staffs.Count + 1 } : staff with { Version = staff.Version + 1 };
        staffs[saved.Id] = saved;
        return Task.FromResult(saved);
    }

    public Task<bool> UpdateRoleAsync(long id, int role, long updatedBy, CancellationToken ct = default)
    {
        UpdateRoleCallCount++;
        if (!staffs.TryGetValue(id, out var s) || s.DeletedAt is not null) return Task.FromResult(false);
        staffs[id] = s with { Role = role, UpdatedBy = updatedBy };
        return Task.FromResult(true);
    }

    public Task<bool> SoftDeleteAsync(long id, long deletedBy, int version, CancellationToken ct = default)
    {
        SoftDeleteCallCount++;
        LastSoftDeletedId = id;
        LastSoftDeletedVersion = version;
        if (!staffs.TryGetValue(id, out var s) || s.Version != version) return Task.FromResult(false);
        staffs[id] = s with { DeletedAt = DateTime.Now, DeletedBy = deletedBy };
        return Task.FromResult(true);
    }

    public Task<bool> RestoreAsync(long id, CancellationToken ct = default)
    {
        RestoreCallCount++;
        if (!staffs.TryGetValue(id, out var s) || s.DeletedAt is null) return Task.FromResult(false);
        staffs[id] = s with { DeletedAt = null, DeletedBy = null };
        return Task.FromResult(true);
    }
}
