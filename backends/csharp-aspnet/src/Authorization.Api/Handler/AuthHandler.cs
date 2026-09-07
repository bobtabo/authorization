/*
 * 認証ハンドラーモジュール。
 *
 * OAuth（Google / GitHub）のリダイレクト・コールバック、ログイン状態の取得、ログアウト、招待確認を扱います。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Text.Json;
using Authorization.Api.Config;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Auth;
using Authorization.Api.UseCase.Invitation;
using static Authorization.Api.Handler.HttpHelpers;

namespace Authorization.Api.Handler;

/// <summary>OAuth プロバイダーから取得したユーザー情報です。</summary>
public sealed record OAuthUserInfo(string Id, string Name, string Email, string? Avatar);

/// <summary>OAuth プロバイダーとの HTTP 通信です。</summary>
public interface IOAuthClient
{
    Task<string> ExchangeGoogleCodeAsync(string code, CancellationToken ct);
    Task<OAuthUserInfo> FetchGoogleUserInfoAsync(string accessToken, CancellationToken ct);
    Task<string> ExchangeGithubCodeAsync(string code, CancellationToken ct);
    Task<OAuthUserInfo> FetchGithubUserInfoAsync(string accessToken, CancellationToken ct);
}

/// <summary>HttpClient による OAuth クライアントです。</summary>
public sealed class HttpOAuthClient(HttpClient http, OAuthSettings oauth) : IOAuthClient
{
    public async Task<string> ExchangeGoogleCodeAsync(string code, CancellationToken ct)
    {
        using var res = await http.PostAsync("https://oauth2.googleapis.com/token", new FormUrlEncodedContent(
        [
            new("code",          code),
            new("client_id",     oauth.GoogleClientId),
            new("client_secret", oauth.GoogleClientSecret),
            new("redirect_uri",  oauth.GoogleRedirectUrl),
            new("grant_type",    "authorization_code"),
        ]), ct);
        res.EnsureSuccessStatusCode();
        using var doc = JsonDocument.Parse(await res.Content.ReadAsStringAsync(ct));
        return doc.RootElement.TryGetProperty("access_token", out var t) && t.ValueKind == JsonValueKind.String
            ? t.GetString()! : throw new InvalidOperationException("no access_token");
    }

    public async Task<OAuthUserInfo> FetchGoogleUserInfoAsync(string accessToken, CancellationToken ct)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, "https://www.googleapis.com/oauth2/v2/userinfo");
        req.Headers.Authorization = new("Bearer", accessToken);
        using var res = await http.SendAsync(req, ct);
        res.EnsureSuccessStatusCode();
        using var doc = JsonDocument.Parse(await res.Content.ReadAsStringAsync(ct));
        var root = doc.RootElement;
        var picture = Prop(root, "picture");
        return new OAuthUserInfo(Prop(root, "id"), Prop(root, "name"), Prop(root, "email"),
            picture.Length == 0 ? null : picture);
    }

    public async Task<string> ExchangeGithubCodeAsync(string code, CancellationToken ct)
    {
        using var req = new HttpRequestMessage(HttpMethod.Post, "https://github.com/login/oauth/access_token")
        {
            Content = new FormUrlEncodedContent(
            [
                new("client_id",     oauth.GithubClientId),
                new("client_secret", oauth.GithubClientSecret),
                new("code",          code),
            ]),
        };
        req.Headers.Accept.Add(new("application/json"));
        using var res = await http.SendAsync(req, ct);
        res.EnsureSuccessStatusCode();
        using var doc = JsonDocument.Parse(await res.Content.ReadAsStringAsync(ct));
        return doc.RootElement.TryGetProperty("access_token", out var t) && t.ValueKind == JsonValueKind.String
            ? t.GetString()! : throw new InvalidOperationException("no access_token");
    }

    public async Task<OAuthUserInfo> FetchGithubUserInfoAsync(string accessToken, CancellationToken ct)
    {
        using var userDoc = await GetGithubJsonAsync("https://api.github.com/user", accessToken, ct);
        var root  = userDoc.RootElement;
        var name  = Prop(root, "name");
        if (name.Length == 0) name = Prop(root, "login");
        var id    = Prop(root, "id");
        var email = Prop(root, "email");

        if (email.Length == 0)
        {
            using var emailsDoc = await GetGithubJsonAsync("https://api.github.com/user/emails", accessToken, ct);
            if (emailsDoc.RootElement.ValueKind == JsonValueKind.Array)
            {
                foreach (var e in emailsDoc.RootElement.EnumerateArray())
                {
                    if (e.TryGetProperty("primary", out var p) && p.ValueKind == JsonValueKind.True)
                    {
                        email = Prop(e, "email");
                        break;
                    }
                }
            }
        }

        var avatar = Prop(root, "avatar_url");
        return new OAuthUserInfo(id, name, email, avatar.Length == 0 ? null : avatar);
    }

    private async Task<JsonDocument> GetGithubJsonAsync(string url, string accessToken, CancellationToken ct)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        req.Headers.Authorization = new("Bearer", accessToken);
        req.Headers.Accept.Add(new("application/json"));
        req.Headers.UserAgent.ParseAdd("authorization-csharp");
        using var res = await http.SendAsync(req, ct);
        res.EnsureSuccessStatusCode();
        return JsonDocument.Parse(await res.Content.ReadAsStringAsync(ct));
    }

    private static string Prop(JsonElement el, string key)
    {
        if (!el.TryGetProperty(key, out var v)) return "";
        return v.ValueKind switch
        {
            JsonValueKind.String => v.GetString() ?? "",
            JsonValueKind.Number => v.GetRawText(),
            _                    => "",
        };
    }
}

/// <summary>認証ハンドラーです。</summary>
public sealed class AuthHandler(
    AuthInteractor authUC,
    InvitationInteractor invitationUC,
    IOAuthClient oauthClient,
    AppConfig cfg,
    ILogger<AuthHandler> logger)
{
    private string ErrorUrl(int code) => $"{cfg.App.FrontendUrl}/error?code={code}";

    public IResult GoogleRedirect(HttpRequest req)
    {
        var token = Query(req, "token");
        var state = string.IsNullOrEmpty(token) ? "state" : token;
        var url = "https://accounts.google.com/o/oauth2/auth" +
                  $"?client_id={cfg.OAuth.GoogleClientId}" +
                  $"&redirect_uri={cfg.OAuth.GoogleRedirectUrl}" +
                  "&response_type=code&scope=email+profile&access_type=online" +
                  $"&state={Uri.EscapeDataString(state)}";
        return Results.Redirect(url);
    }

    public async Task<IResult> GoogleCallbackAsync(HttpRequest req, CancellationToken ct)
    {
        var code = Query(req, "code");
        if (string.IsNullOrEmpty(code)) return Results.Redirect(ErrorUrl(500));

        var state = Query(req, "state");
        var invitationToken = !string.IsNullOrEmpty(state) && state != "state" ? state : null;

        OAuthUserInfo info;
        try
        {
            var accessToken = await oauthClient.ExchangeGoogleCodeAsync(code, ct);
            info = await oauthClient.FetchGoogleUserInfoAsync(accessToken, ct);
        }
        catch (Exception e)
        {
            logger.LogWarning(e, "google oauth failed");
            return Results.Redirect(ErrorUrl(500));
        }

        return await LoginAndRedirectAsync(new LoginDto(StaffProvider.Google, info.Id, info.Name, info.Email, info.Avatar, invitationToken), ct);
    }

    public IResult GithubRedirect(HttpRequest req)
    {
        var token = Query(req, "token");
        var state = string.IsNullOrEmpty(token)
            ? Uri.EscapeDataString(cfg.App.Runtime)
            : Uri.EscapeDataString($"{cfg.App.Runtime}|{token}");
        var url = "https://github.com/login/oauth/authorize" +
                  $"?client_id={cfg.OAuth.GithubClientId}" +
                  $"&redirect_uri={Uri.EscapeDataString(cfg.OAuth.GithubRedirectUrl)}" +
                  "&scope=user:email" +
                  $"&state={state}";
        return Results.Redirect(url);
    }

    public async Task<IResult> GithubCallbackAsync(HttpRequest req, CancellationToken ct)
    {
        var code = Query(req, "code");
        if (string.IsNullOrEmpty(code)) return Results.Redirect(ErrorUrl(500));

        var parts = (Query(req, "state") ?? "").Split('|', 2);
        var invitationToken = parts.Length == 2 && parts[1].Length > 0 ? parts[1] : null;

        OAuthUserInfo info;
        try
        {
            var accessToken = await oauthClient.ExchangeGithubCodeAsync(code, ct);
            info = await oauthClient.FetchGithubUserInfoAsync(accessToken, ct);
        }
        catch (Exception e)
        {
            logger.LogWarning(e, "github oauth failed");
            return Results.Redirect(ErrorUrl(500));
        }

        return await LoginAndRedirectAsync(new LoginDto(StaffProvider.Github, info.Id, info.Name, info.Email, info.Avatar, invitationToken), ct);
    }

    private async Task<IResult> LoginAndRedirectAsync(LoginDto dto, CancellationToken ct)
    {
        Domain.Staff.Staff staff;
        try
        {
            staff = await authUC.LoginAsync(dto, ct);
        }
        catch (AppException e)
        {
            return Results.Redirect(ErrorUrl(e.StatusCode == 403 ? 403 : 500));
        }
        catch (Exception e)
        {
            logger.LogError(e, "login failed");
            return Results.Redirect(ErrorUrl(500));
        }

        return new CookieRedirectResult(staff.Id, cfg.App, $"{cfg.App.FrontendUrl}/clients");
    }

    /// <summary>ログイン中スタッフのプロフィールを返します（/auth/me, /auth/login 共通）。</summary>
    public async Task<IResult> ProfileAsync(HttpRequest req, CancellationToken ct)
    {
        var staffId = StaffId(req);
        if (staffId == 0) return Unauthenticated();

        var s = await authUC.FindUserAsync(staffId, ct);
        return Results.Json(new Dictionary<string, object?>
        {
            ["staff_id"] = s.Id,
            ["name"]     = s.Name,
            ["avatar"]   = s.Avatar,
            ["role"]     = s.Role,
        });
    }

    public IResult Logout(HttpResponse res)
    {
        res.Cookies.Append("staff_id", "", new CookieOptions { MaxAge = TimeSpan.Zero, Path = "/", HttpOnly = true });
        return Empty();
    }

    public async Task<IResult> InvitationAsync(string token, CancellationToken ct)
    {
        var v = await invitationUC.FindByTokenAsync(token, ct);
        return Results.Json(InvitationJson(v));
    }

    /// <summary>招待レスポンス JSON を組み立てます。</summary>
    public static Dictionary<string, object?> InvitationJson(Domain.Invitation.InvitationVo v) => new()
    {
        ["found"]       = true,
        ["url"]         = v.Url,
        ["display_url"] = v.DisplayUrl,
        ["token"]       = v.Token,
    };

    /// <summary>staff_id クッキーを付与してリダイレクトする結果です。</summary>
    private sealed class CookieRedirectResult(long staffId, AppSettings app, string location) : IResult
    {
        public Task ExecuteAsync(HttpContext ctx)
        {
            ctx.Response.Cookies.Append("staff_id", staffId.ToString(), new CookieOptions
            {
                MaxAge   = TimeSpan.FromMinutes(app.StaffCookieLifetime),
                Path     = "/",
                Secure   = app.Env == "production",
                HttpOnly = true,
            });
            ctx.Response.Redirect(location);
            return Task.CompletedTask;
        }
    }
}
