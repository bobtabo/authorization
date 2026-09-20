// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;
using Authorization.Api.Domain.Client;

namespace Authorization.Api.Tests.Integration;

/// <summary>クライアントAPIの統合テストです（実MySQL/Redis）。</summary>
public class ClientIntegrationTests(IntegrationWebAppFactory factory) : IntegrationTestBase(factory)
{
    [Fact]
    public async Task Index_WithClients_ReturnsList()
    {
        TestHelper.CreateClient(name: "Client A", identifier: "client-a", accessToken: "token-a");
        TestHelper.CreateClient(name: "Client B", identifier: "client-b", accessToken: "token-b");

        var res  = await SendAsync(HttpMethod.Get, "/api/clients");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(2, json.GetProperty("data").GetArrayLength());
    }

    [Fact]
    public async Task Index_Empty_ReturnsEmptyList()
    {
        var res  = await SendAsync(HttpMethod.Get, "/api/clients");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(0, json.GetProperty("data").GetArrayLength());
    }

    [Fact]
    public async Task Index_KeywordPercent_IsNotTreatedAsWildcard()
    {
        TestHelper.CreateClient(name: "50%割引プラン", identifier: "client-percent", accessToken: "token-percent");
        TestHelper.CreateClient(name: "50個セット", identifier: "client-nomatch", accessToken: "token-nomatch");

        var res  = await SendAsync(HttpMethod.Get, "/api/clients?keyword=50%25");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(1, json.GetProperty("data").GetArrayLength());
    }

    [Fact]
    public async Task Show_Existing_ReturnsDetail()
    {
        var id = TestHelper.CreateClient(name: "Detail Client");

        var res  = await SendAsync(HttpMethod.Get, $"/api/clients/{id}");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(id, json.GetProperty("id").GetInt64());
        Assert.Equal("Detail Client", json.GetProperty("name").GetString());
        Assert.Equal("client-1", json.GetProperty("identifier").GetString());
    }

    [Fact]
    public async Task Show_Missing_Returns404()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/clients/99999");

        Assert.Equal(HttpStatusCode.NotFound, res.StatusCode);
    }

    [Fact]
    public async Task Store_Valid_Returns201()
    {
        var staffId = TestHelper.CreateStaff();

        var res = await SendAsync(HttpMethod.Post, "/api/clients/store", staffId, new
        {
            name      = "New Client",
            post_code = "1000001",
            pref      = "東京都",
            city      = "千代田区",
            address   = "1-1-1",
            tel       = "0312345678",
            email     = "new@example.com",
        });
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.Created, res.StatusCode);
        Assert.True(json.GetProperty("id").GetInt64() > 0);

        using var db = TestHelper.NewDbContext();
        Assert.Equal(1, db.Clients.Count());
        Assert.Equal(1, db.Notifications.Count(n => n.StaffId == staffId));
    }

    [Fact]
    public async Task Update_Valid_ReturnsUpdatedDetail()
    {
        var id = TestHelper.CreateClient(name: "Old Name");

        var res  = await SendAsync(HttpMethod.Put, $"/api/clients/{id}/update", body: new { name = "New Name", version = 1 });
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal("New Name", json.GetProperty("name").GetString());
        Assert.Equal(2, json.GetProperty("version").GetInt32());
    }

    [Fact]
    public async Task Destroy_Existing_SoftDeletes()
    {
        var id = TestHelper.CreateClient();

        var res = await SendAsync(HttpMethod.Delete, $"/api/clients/{id}/delete", body: new { version = 1 });

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        using var db = TestHelper.NewDbContext();
        var client = db.Clients.Single(c => c.Id == id);
        Assert.NotNull(client.DeletedAt);
        Assert.Equal(ClientStatus.Closed, client.Status);
    }

    [Fact]
    public async Task Qr_Existing_ReturnsDeeplink()
    {
        TestHelper.CreateClient(identifier: "qr-client");

        var res  = await SendAsync(HttpMethod.Get, "/api/clients/qr-client/qr");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal("qr-client", json.GetProperty("identifier").GetString());
        Assert.False(string.IsNullOrEmpty(json.GetProperty("deeplink_url").GetString()));
    }

    [Fact]
    public async Task Qr_Missing_Returns404()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/clients/missing/qr");

        Assert.Equal(HttpStatusCode.NotFound, res.StatusCode);
    }

    [Fact]
    public async Task Info_Existing_ReturnsInfo()
    {
        TestHelper.CreateClient(name: "Info Client", identifier: "info-client");

        var res  = await SendAsync(HttpMethod.Get, "/api/clients/info-client/info");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal("Info Client", json.GetProperty("name").GetString());
        Assert.Equal(ClientStatus.Active, json.GetProperty("status").GetInt32());
    }

    [Fact]
    public async Task Info_Missing_Returns404()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/clients/missing/info");

        Assert.Equal(HttpStatusCode.NotFound, res.StatusCode);
    }

    [Fact]
    public async Task Start_Inactive_ReturnsAccessToken()
    {
        TestHelper.CreateClient(identifier: "start-client", accessToken: "start-token", status: ClientStatus.Inactive);

        var res  = await SendAsync(HttpMethod.Patch, "/api/clients/start-client/start");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal("start-token", json.GetProperty("access_token").GetString());
        using var db = TestHelper.NewDbContext();
        Assert.Equal(ClientStatus.Active, db.Clients.Single(c => c.Identifier == "start-client").Status);
    }

    [Fact]
    public async Task Start_Missing_Returns404()
    {
        var res = await SendAsync(HttpMethod.Patch, "/api/clients/missing/start");

        Assert.Equal(HttpStatusCode.NotFound, res.StatusCode);
    }

    [Fact]
    public async Task Stop_Active_Suspends()
    {
        TestHelper.CreateClient(identifier: "stop-client");

        var res = await SendAsync(HttpMethod.Patch, "/api/clients/stop-client/stop");

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        using var db = TestHelper.NewDbContext();
        Assert.Equal(ClientStatus.Suspended, db.Clients.Single(c => c.Identifier == "stop-client").Status);
    }

    [Fact]
    public async Task Stop_Missing_Returns404()
    {
        var res = await SendAsync(HttpMethod.Patch, "/api/clients/missing/stop");

        Assert.Equal(HttpStatusCode.NotFound, res.StatusCode);
    }
}
