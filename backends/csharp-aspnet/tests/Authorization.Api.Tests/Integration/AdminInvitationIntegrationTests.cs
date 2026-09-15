// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;
using Authorization.Api.Domain.Staff;

namespace Authorization.Api.Tests.Integration;

/// <summary>管理者向け招待APIの統合テストです（実MySQL/Redis）。</summary>
public class AdminInvitationIntegrationTests(IntegrationWebAppFactory factory) : IntegrationTestBase(factory)
{
    [Fact]
    public async Task Index_Existing_ReturnsCurrentInvitation()
    {
        TestHelper.CreateInvitation(token: "current-token", role: StaffRole.Member);

        var res  = await SendAsync(HttpMethod.Get, "/api/admin/invitation");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal("current-token", json.GetProperty("token").GetString());
    }

    [Fact]
    public async Task Issue_Authenticated_CreatesInvitation()
    {
        var staffId = TestHelper.CreateStaff();

        var res  = await SendAsync(HttpMethod.Get, $"/api/admin/invitation/issue?role={StaffRole.Admin}", staffId);
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        var token = json.GetProperty("token").GetString();
        Assert.False(string.IsNullOrEmpty(token));
        using var db = TestHelper.NewDbContext();
        Assert.Equal(StaffRole.Admin, db.Invitations.Single(i => i.Token == token).Role);
    }

    [Fact]
    public async Task Issue_Unauthenticated_Returns401()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/admin/invitation/issue");

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Issue_InvalidRole_Returns400()
    {
        var staffId = TestHelper.CreateStaff();

        var res = await SendAsync(HttpMethod.Get, "/api/admin/invitation/issue?role=99", staffId);

        Assert.Equal(HttpStatusCode.BadRequest, res.StatusCode);
    }
}
