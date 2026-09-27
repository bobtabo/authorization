// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net;
using Authorization.Api.Handler;

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
    public async Task Me_UnsignedForgedCookie_Returns401()
    {
        var staffId = TestHelper.CreateStaff(email: "forge-1@example.com");
        // 署名を付けず staff_id をそのまま設定した「偽造」クッキー。
        var req = new HttpRequestMessage(HttpMethod.Get, "/api/auth/me");
        req.Headers.Add("Cookie", $"staff_id={staffId}");
        var res = await Client.SendAsync(req);

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Me_TamperedSignature_Returns401()
    {
        var staffId = TestHelper.CreateStaff(email: "forge-2@example.com");
        var signed  = SignStaffCookie(staffId);
        // 末尾の1文字を必ず異なる値に置き換える（元の値と偶然一致すると署名が
        // 変わらずテストが不安定になるため）。
        var replacement = signed[^1] == '0' ? '1' : '0';
        var tampered = signed[..^1] + replacement;
        var req = new HttpRequestMessage(HttpMethod.Get, "/api/auth/me");
        req.Headers.Add("Cookie", $"staff_id={tampered}");
        var res = await Client.SendAsync(req);

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Me_SignedWithAnotherSecret_Returns401()
    {
        var staffId = TestHelper.CreateStaff(email: "forge-3@example.com");
        var forged  = StaffSession.SignStaffId(staffId, "attacker-controlled-secret", TimeSpan.FromHours(1));
        var req = new HttpRequestMessage(HttpMethod.Get, "/api/auth/me");
        req.Headers.Add("Cookie", $"staff_id={forged}");
        var res = await Client.SendAsync(req);

        Assert.Equal(HttpStatusCode.Unauthorized, res.StatusCode);
    }

    [Fact]
    public async Task Me_ExpiredSignedCookie_Returns401()
    {
        var staffId = TestHelper.CreateStaff(email: "forge-4@example.com");
        // 署名自体は正しいが、Max-Ageが切れた後に手動でCookieヘッダーを
        // 再送した状況を再現する（署名対象に有効期限を含めていないと防げない）。
        var expired = StaffSession.SignStaffId(staffId, TestHelper.Config.App.StaffCookieSecret, TimeSpan.FromHours(-1));
        var req = new HttpRequestMessage(HttpMethod.Get, "/api/auth/me");
        req.Headers.Add("Cookie", $"staff_id={expired}");
        var res = await Client.SendAsync(req);

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
    public async Task GithubRedirect_IssuesNonceCookieAndEmbedsItInState()
    {
        using var client = CreateNoRedirectClient();
        var res = await client.GetAsync("/auth/github/redirect?token=inv-token");

        Assert.Equal(HttpStatusCode.Redirect, res.StatusCode);
        var cookie = res.Headers.GetValues("Set-Cookie").Single(c => c.StartsWith("oauth_state="));
        var nonce  = System.Text.RegularExpressions.Regex.Match(cookie, "oauth_state=([0-9a-f]+)").Groups[1].Value;
        Assert.NotEmpty(nonce);
        Assert.Contains("httponly", cookie, StringComparison.OrdinalIgnoreCase);
        var location = res.Headers.Location!.OriginalString;
        Assert.Contains($"state=csharp%7C{nonce}%7Cinv-token", location, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public async Task GithubCallback_WithoutNonceCookie_RedirectsTo400()
    {
        using var client = CreateNoRedirectClient();
        var res = await client.GetAsync("/auth/github/callback?code=abc&state=csharp%7Cnonce123");

        Assert.Equal(HttpStatusCode.Redirect, res.StatusCode);
        Assert.EndsWith("/error?code=400", res.Headers.Location!.ToString());
    }

    [Fact]
    public async Task GoogleCallback_WithMismatchedNonce_RedirectsTo400AndClearsCookie()
    {
        using var client = CreateNoRedirectClient();
        var req = new HttpRequestMessage(HttpMethod.Get, "/auth/google/callback?code=abc&state=csharp%7Cwrong");
        req.Headers.Add("Cookie", "oauth_state=right");
        var res = await client.SendAsync(req);

        Assert.Equal(HttpStatusCode.Redirect, res.StatusCode);
        Assert.EndsWith("/error?code=400", res.Headers.Location!.ToString());
        var cookie = res.Headers.GetValues("Set-Cookie").Single(c => c.StartsWith("oauth_state="));
        Assert.Contains("max-age=0", cookie, StringComparison.OrdinalIgnoreCase);
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
