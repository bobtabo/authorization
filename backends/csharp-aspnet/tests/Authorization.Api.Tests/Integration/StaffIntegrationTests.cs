// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;
using Authorization.Api.Domain.Staff;

namespace Authorization.Api.Tests.Integration;

/// <summary>スタッフAPIの統合テストです（実MySQL/Redis）。</summary>
public class StaffIntegrationTests(IntegrationWebAppFactory factory) : IntegrationTestBase(factory)
{
    [Fact]
    public async Task Index_WithStaffs_ReturnsList()
    {
        TestHelper.CreateStaff(name: "Staff A", email: "a@example.com", providerId: "google-a");
        TestHelper.CreateStaff(name: "Staff B", email: "b@example.com", providerId: "google-b", role: StaffRole.Member);

        var res  = await SendAsync(HttpMethod.Get, "/api/staffs");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(2, json.GetProperty("data").GetArrayLength());
    }

    [Fact]
    public async Task Index_Empty_ReturnsEmptyList()
    {
        var res  = await SendAsync(HttpMethod.Get, "/api/staffs");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(0, json.GetProperty("data").GetArrayLength());
    }

    [Fact]
    public async Task Index_KeywordUnderscore_IsNotTreatedAsWildcard()
    {
        TestHelper.CreateStaff(name: "アンダースコア", email: "a_b@example.com", providerId: "google-underscore");
        TestHelper.CreateStaff(name: "エックス", email: "axb@example.com", providerId: "google-x");

        var res  = await SendAsync(HttpMethod.Get, "/api/staffs?keyword=a_b");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(1, json.GetProperty("data").GetArrayLength());
    }

    [Fact]
    public async Task UpdateRole_Valid_UpdatesRole()
    {
        var adminId  = TestHelper.CreateStaff(name: "Admin", email: "admin@example.com", providerId: "google-admin");
        var targetId = TestHelper.CreateStaff(name: "Target", email: "target@example.com", providerId: "google-target", role: StaffRole.Member);

        var res  = await SendAsync(HttpMethod.Patch, $"/api/staffs/{targetId}/updateRole", adminId, new { role = StaffRole.Admin });
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(targetId, json.GetProperty("id").GetInt64());
        using var db = TestHelper.NewDbContext();
        Assert.Equal(StaffRole.Admin, db.Staffs.Single(s => s.Id == targetId).Role);
    }

    [Fact]
    public async Task UpdateRole_Unauthenticated_Returns401()
    {
        var targetId = TestHelper.CreateStaff(name: "Target", email: "target-unauth@example.com", providerId: "google-target-unauth", role: StaffRole.Member);

        var res = await SendAsync(HttpMethod.Patch, $"/api/staffs/{targetId}/updateRole", staffId: null, new { role = StaffRole.Admin });

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task UpdateRole_NonAdminExecutor_Returns403()
    {
        var executorId = TestHelper.CreateStaff(name: "Member Executor", email: "member-executor@example.com", providerId: "google-member-executor", role: StaffRole.Member);
        var targetId   = TestHelper.CreateStaff(name: "Target", email: "target-member@example.com", providerId: "google-target-member", role: StaffRole.Member);

        var res = await SendAsync(HttpMethod.Patch, $"/api/staffs/{targetId}/updateRole", executorId, new { role = StaffRole.Admin });

        Assert.Equal(HttpStatusCode.Forbidden, res.StatusCode);
    }

    [Fact]
    public async Task UpdateRole_DeletedAdminExecutor_Returns403()
    {
        var executorId = TestHelper.CreateStaff(name: "Deleted Admin", email: "deleted-admin-executor@example.com", providerId: "google-deleted-admin", deleted: true);
        var targetId   = TestHelper.CreateStaff(name: "Target", email: "target-deleted-admin@example.com", providerId: "google-target-deleted-admin", role: StaffRole.Member);

        var res = await SendAsync(HttpMethod.Patch, $"/api/staffs/{targetId}/updateRole", executorId, new { role = StaffRole.Admin });

        Assert.Equal(HttpStatusCode.Forbidden, res.StatusCode);
    }

    [Fact]
    public async Task Restore_Deleted_ClearsDeletedAt()
    {
        var id = TestHelper.CreateStaff(deleted: true);

        var res = await SendAsync(HttpMethod.Patch, $"/api/staffs/{id}/restore");

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        using var db = TestHelper.NewDbContext();
        Assert.Null(db.Staffs.Single(s => s.Id == id).DeletedAt);
    }

    [Fact]
    public async Task Destroy_Existing_SoftDeletes()
    {
        var adminId  = TestHelper.CreateStaff(name: "Admin", email: "admin@example.com", providerId: "google-admin");
        var targetId = TestHelper.CreateStaff(name: "Target", email: "target@example.com", providerId: "google-target", role: StaffRole.Member);

        var res = await SendAsync(HttpMethod.Delete, $"/api/staffs/{targetId}/delete", adminId);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        using var db = TestHelper.NewDbContext();
        Assert.NotNull(db.Staffs.Single(s => s.Id == targetId).DeletedAt);
    }
}
