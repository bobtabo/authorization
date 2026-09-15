// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;

namespace Authorization.Api.Tests.Integration;

/// <summary>通知APIの統合テストです（実MySQL/Redis）。</summary>
public class NotificationIntegrationTests(IntegrationWebAppFactory factory) : IntegrationTestBase(factory)
{
    [Fact]
    public async Task Counts_Authenticated_ReturnsCounts()
    {
        var staffId = TestHelper.CreateStaff();
        TestHelper.CreateNotification(staffId, title: "N1");
        TestHelper.CreateNotification(staffId, title: "N2", read: true);

        var res  = await SendAsync(HttpMethod.Get, "/api/notifications/counts", staffId);
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(1, json.GetProperty("unread").GetInt64());
        Assert.Equal(2, json.GetProperty("total").GetInt64());
    }

    [Fact]
    public async Task Counts_Unauthenticated_Returns401()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/notifications/counts");

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Index_Authenticated_ReturnsItems()
    {
        var staffId = TestHelper.CreateStaff();
        TestHelper.CreateNotification(staffId, title: "N1");
        TestHelper.CreateNotification(staffId, title: "N2");

        var res  = await SendAsync(HttpMethod.Get, "/api/notifications", staffId);
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(2, json.GetProperty("items").GetArrayLength());
    }

    [Fact]
    public async Task Index_Unauthenticated_Returns401()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/notifications");

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task ReadAll_Authenticated_MarksAllRead()
    {
        var staffId = TestHelper.CreateStaff();
        TestHelper.CreateNotification(staffId, title: "N1");
        TestHelper.CreateNotification(staffId, title: "N2");

        var res = await SendAsync(HttpMethod.Patch, "/api/notifications", staffId);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        using var db = TestHelper.NewDbContext();
        Assert.Equal(0, db.Notifications.Count(n => n.StaffId == staffId && !n.Read));
    }

    [Fact]
    public async Task ReadAll_Unauthenticated_Returns401()
    {
        var res = await SendAsync(HttpMethod.Patch, "/api/notifications");

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Read_Single_MarksRead()
    {
        var staffId = TestHelper.CreateStaff();
        var id      = TestHelper.CreateNotification(staffId);

        var res  = await SendAsync(HttpMethod.Patch, $"/api/notifications/{id}", staffId);
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(id, json.GetProperty("id").GetInt64());
        using var db = TestHelper.NewDbContext();
        Assert.True(db.Notifications.Single(n => n.Id == id).Read);
    }
}
