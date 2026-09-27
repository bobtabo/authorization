// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;
using System.Net.Http.Headers;

namespace Authorization.Api.Tests.Integration;

/// <summary>Gate（JWT発行・検証）APIの統合テストです（実MySQL/Redis）。</summary>
public class GateIntegrationTests(IntegrationWebAppFactory factory) : IntegrationTestBase(factory)
{
    private Task<HttpResponseMessage> IssueAsync(string accessToken, string member)
    {
        var req = new HttpRequestMessage(HttpMethod.Get, $"/api/gate/issue?member={member}");
        req.Headers.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);
        return Client.SendAsync(req);
    }

    [Fact]
    public async Task Issue_ValidClient_ReturnsToken()
    {
        TestHelper.CreateClient(identifier: "gate-client", accessToken: "gate-token");

        var res  = await IssueAsync("gate-token", "member-1");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.False(string.IsNullOrEmpty(json.GetProperty("token").GetString()));

        using var db = TestHelper.NewDbContext();
        Assert.Equal(1, db.JwtHistories.Count(h => h.MemberId == "member-1"));
        var redis = TestHelper.Redis.GetServer(TestHelper.Redis.GetEndPoints()[0]);
        Assert.NotEmpty(redis.Keys(TestHelper.Config.Redis.Database, "*gate.jwt:gate-client:member-1"));
    }

    [Fact]
    public async Task Issue_MissingMember_Returns400()
    {
        TestHelper.CreateClient(identifier: "gate-client", accessToken: "gate-token");

        var res = await IssueAsync("gate-token", "");

        Assert.Equal(HttpStatusCode.BadRequest, res.StatusCode);
    }

    [Fact]
    public async Task Verify_ValidToken_ReturnsClaims()
    {
        TestHelper.CreateClient(identifier: "gate-client", accessToken: "gate-token");
        var issued = await ReadJsonAsync(await IssueAsync("gate-token", "member-1"));
        var token  = issued.GetProperty("token").GetString();

        var res  = await SendAsync(HttpMethod.Get, $"/api/gate/client/gate-client/verify?token={token}");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal("member-1", json.GetProperty("sub").GetString());
    }

    [Fact]
    public async Task Verify_MissingToken_Returns400()
    {
        TestHelper.CreateClient(identifier: "gate-client", accessToken: "gate-token");

        var res = await SendAsync(HttpMethod.Get, "/api/gate/client/gate-client/verify");

        Assert.Equal(HttpStatusCode.BadRequest, res.StatusCode);
    }
}
