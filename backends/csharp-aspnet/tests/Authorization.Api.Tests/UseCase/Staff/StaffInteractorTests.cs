using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Staff;

namespace Authorization.Api.Tests.UseCase.Staff;

public class StaffInteractorTests
{
    private static Authorization.Api.Domain.Staff.Staff MakeStaff(
        long id = 1, int role = StaffRole.Member, int version = 1, DateTime? deletedAt = null) => new()
    {
        Id        = id,
        Name      = $"Staff {id}",
        Email     = $"staff{id}@example.com",
        Role      = role,
        Version   = version,
        DeletedAt = deletedAt,
    };

    [Fact]
    public async Task FindByConditionWithCountAsync_MapsToListItemAndReturnsCount()
    {
        var repo = new FakeStaffRepository().Add(MakeStaff(1)).Add(MakeStaff(2, role: StaffRole.Admin));
        var uc   = new StaffInteractor(repo);

        var (items, count) = await uc.FindByConditionWithCountAsync(new StaffCondition());

        Assert.Equal(2, count);
        Assert.Equal(2, items.Count);
        Assert.Contains(items, i => i.Id == 2 && i.Role == StaffRole.Admin);
    }

    [Fact]
    public async Task UpdateRoleAsync_InvalidRole_ThrowsBadRequest()
    {
        var repo = new FakeStaffRepository().Add(MakeStaff(1));
        var uc   = new StaffInteractor(repo);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.UpdateRoleAsync(new StaffUpdateRoleDto(1, Role: 999, ExecutorId: 9)));

        Assert.Equal(400, ex.StatusCode);
        Assert.Equal(0, repo.UpdateRoleCallCount);
    }

    [Fact]
    public async Task UpdateRoleAsync_StaffNotFound_ThrowsNotFound()
    {
        var repo = new FakeStaffRepository();
        var uc   = new StaffInteractor(repo);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.UpdateRoleAsync(new StaffUpdateRoleDto(999, Role: StaffRole.Admin, ExecutorId: 9)));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task UpdateRoleAsync_DeletedStaff_ThrowsNotFound()
    {
        var repo = new FakeStaffRepository().Add(MakeStaff(1, deletedAt: DateTime.Now));
        var uc   = new StaffInteractor(repo);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.UpdateRoleAsync(new StaffUpdateRoleDto(1, Role: StaffRole.Admin, ExecutorId: 9)));

        Assert.Equal(404, ex.StatusCode);
        Assert.Equal(0, repo.UpdateRoleCallCount);
    }

    [Fact]
    public async Task UpdateRoleAsync_ValidRequest_DelegatesToRepository()
    {
        var repo = new FakeStaffRepository().Add(MakeStaff(1));
        var uc   = new StaffInteractor(repo);

        await uc.UpdateRoleAsync(new StaffUpdateRoleDto(1, Role: StaffRole.Admin, ExecutorId: 9));

        Assert.Equal(1, repo.UpdateRoleCallCount);
    }

    [Fact]
    public async Task RestoreAsync_NotFound_ThrowsNotFound()
    {
        var repo = new FakeStaffRepository();
        var uc   = new StaffInteractor(repo);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.RestoreAsync(1));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task RestoreAsync_DeletedStaff_Succeeds()
    {
        var repo = new FakeStaffRepository().Add(MakeStaff(1, deletedAt: DateTime.Now));
        var uc   = new StaffInteractor(repo);

        await uc.RestoreAsync(1);

        Assert.Equal(1, repo.RestoreCallCount);
    }

    [Fact]
    public async Task DestroyAsync_NotFound_ThrowsNotFound()
    {
        var repo = new FakeStaffRepository();
        var uc   = new StaffInteractor(repo);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.DestroyAsync(new StaffDestroyDto(1, ExecutorId: 9)));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task DestroyAsync_ValidRequest_SoftDeletesWithCurrentVersion()
    {
        var repo = new FakeStaffRepository().Add(MakeStaff(1, version: 3));
        var uc   = new StaffInteractor(repo);

        await uc.DestroyAsync(new StaffDestroyDto(1, ExecutorId: 9));

        Assert.Equal(1, repo.SoftDeleteCallCount);
        Assert.Equal(1L, repo.LastSoftDeletedId);
        Assert.Equal(3, repo.LastSoftDeletedVersion);
    }
}
