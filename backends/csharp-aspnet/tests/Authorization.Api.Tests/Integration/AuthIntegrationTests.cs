// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;

namespace Authorization.Api.Tests.Integration;

/// <summary>認証APIの統合テストです（実MySQL/Redis）。</summary>
public class AuthIntegrationTests(IntegrationWebAppFactory factory) : IntegrationTestBase(factory)
{
    [Fact]
    public async Task Me_Authenticated_ReturnsProfile()
    {
        var staffId = TestHelper.CreateStaff(name: "Auth Staff");

        var res  = await SendAsync(HttpMethod.Get, "/api/auth/me", staffId);
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(staffId, json.GetProperty("staff_id").GetInt64());
        Assert.Equal("Auth Staff", json.GetProperty("name").GetString());
    }

    [Fact]
    public async Task Me_Unauthenticated_Returns401()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/auth/me");

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Login_Authenticated_ReturnsProfile()
    {
        var staffId = TestHelper.CreateStaff();

        var res  = await SendAsync(HttpMethod.Get, "/api/auth/login", staffId);
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.Equal(staffId, json.GetProperty("staff_id").GetInt64());
    }

    [Fact]
    public async Task Login_Unauthenticated_Returns401()
    {
        var res = await SendAsync(HttpMethod.Get, "/api/auth/login");

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Logout_ClearsCookie()
    {
        var staffId = TestHelper.CreateStaff();

        var res = await SendAsync(HttpMethod.Get, "/api/auth/logout", staffId);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.True(res.Headers.TryGetValues("Set-Cookie", out var cookies));
        var cookie = Assert.Single(cookies!, c => c.StartsWith("staff_id=", StringComparison.Ordinal));
        var attrs  = cookie.Split(';').Select(a => a.Trim()).ToList();
        Assert.Equal("staff_id=", attrs[0]);
        Assert.Contains("max-age=0", attrs, StringComparer.OrdinalIgnoreCase);
        Assert.Contains("path=/", attrs, StringComparer.OrdinalIgnoreCase);
        Assert.Contains("httponly", attrs, StringComparer.OrdinalIgnoreCase);
    }

    [Fact]
    public async Task Invitation_ValidToken_ReturnsInvitation()
    {
        TestHelper.CreateInvitation(token: "valid-token");

        var res  = await SendAsync(HttpMethod.Get, "/api/auth/invitation/valid-token");
        var json = await ReadJsonAsync(res);

        Assert.Equal(HttpStatusCode.OK, res.StatusCode);
        Assert.True(json.GetProperty("found").GetBoolean());
        Assert.Equal("valid-token", json.GetProperty("token").GetString());
    }
}
