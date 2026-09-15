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
