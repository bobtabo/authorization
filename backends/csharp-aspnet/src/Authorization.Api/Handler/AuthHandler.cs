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
    /// <summary>Google の認可コードをアクセストークンに交換します。</summary>
    /// <param name="code">認可コード</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>アクセストークン</returns>
    Task<string> ExchangeGoogleCodeAsync(string code, CancellationToken ct);

    /// <summary>Google のアクセストークンでユーザー情報を取得します。</summary>
    /// <param name="accessToken">アクセストークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>ユーザー情報</returns>
    Task<OAuthUserInfo> FetchGoogleUserInfoAsync(string accessToken, CancellationToken ct);

    /// <summary>GitHub の認可コードをアクセストークンに交換します。</summary>
    /// <param name="code">認可コード</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>アクセストークン</returns>
    Task<string> ExchangeGithubCodeAsync(string code, CancellationToken ct);

    /// <summary>GitHub のアクセストークンでユーザー情報を取得します（メールアドレス非公開の場合は別APIで補完）。</summary>
    /// <param name="accessToken">アクセストークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>ユーザー情報</returns>
    Task<OAuthUserInfo> FetchGithubUserInfoAsync(string accessToken, CancellationToken ct);
}

/// <summary>HttpClient による OAuth クライアントです。</summary>
public sealed class HttpOAuthClient(HttpClient http, OAuthSettings oauth) : IOAuthClient
{
    /// <inheritdoc/>
    /// <exception cref="HttpRequestException">HTTPリクエストが失敗した場合</exception>
    /// <exception cref="InvalidOperationException">レスポンスにaccess_tokenが含まれない場合</exception>
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

    /// <inheritdoc/>
    /// <exception cref="HttpRequestException">HTTPリクエストが失敗した場合</exception>
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

    /// <inheritdoc/>
    /// <exception cref="HttpRequestException">HTTPリクエストが失敗した場合</exception>
    /// <exception cref="InvalidOperationException">レスポンスにaccess_tokenが含まれない場合</exception>
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

    /// <inheritdoc/>
    /// <exception cref="HttpRequestException">HTTPリクエストが失敗した場合</exception>
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

    /// <summary>GitHub API を Bearer 認証で GET し、JSON をパースします。</summary>
    /// <param name="url">GitHub APIのURL</param>
    /// <param name="accessToken">アクセストークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>パース済みJSONドキュメント</returns>
    /// <exception cref="HttpRequestException">HTTPリクエストが失敗した場合</exception>
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

    /// <summary>JSON要素から文字列/数値プロパティを取り出します（無い場合は空文字）。</summary>
    /// <param name="el">JSON要素</param>
    /// <param name="key">プロパティ名</param>
    /// <returns>プロパティの文字列表現。存在しない場合や文字列/数値以外の場合は空文字</returns>
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
    /// <summary>フロントエンドのエラーページURLを組み立てます。</summary>
    /// <param name="code">エラーコード（表示用）</param>
    /// <returns>エラーページURL</returns>
    private string ErrorUrl(int code) => $"{cfg.App.FrontendUrl}/error?code={code}";

    /// <summary>Google OAuth の認可画面へリダイレクトします。</summary>
    /// <param name="req">HTTPリクエスト（招待トークンをtokenクエリから取得）</param>
    /// <returns>Google認可画面へのリダイレクト</returns>
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

    /// <summary>Google OAuth のコールバックを処理し、ログインしてクッキーを付与しリダイレクトします。</summary>
    /// <param name="req">HTTPリクエスト（code/stateクエリを使用）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クッキー付与済みのフロントエンドへのリダイレクト、失敗時はエラーページへのリダイレクト</returns>
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

    /// <summary>GitHub OAuth の認可画面へリダイレクトします。</summary>
    /// <param name="req">HTTPリクエスト（招待トークンをtokenクエリから取得）</param>
    /// <returns>GitHub認可画面へのリダイレクト</returns>
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

    /// <summary>GitHub OAuth のコールバックを処理し、ログインしてクッキーを付与しリダイレクトします。</summary>
    /// <param name="req">HTTPリクエスト（code/stateクエリを使用。stateは`runtime|招待トークン`形式）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クッキー付与済みのフロントエンドへのリダイレクト、失敗時はエラーページへのリダイレクト</returns>
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

    /// <summary>ログインを実行し、成功時はクッキーを付与してクライアント一覧へ、失敗時はエラーページへリダイレクトします。</summary>
    /// <param name="dto">OAuthプロバイダー情報・招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>リダイレクト結果</returns>
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
    /// <param name="req">HTTPリクエスト（staff_idをクッキーから取得）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>プロフィールのJSON、未認証の場合は401</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
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

    /// <summary>staff_id クッキーを削除してログアウトします。</summary>
    /// <param name="res">HTTPレスポンス（クッキー削除先）</param>
    /// <returns>空レスポンス</returns>
    public IResult Logout(HttpResponse res)
    {
        res.Cookies.Append("staff_id", "", new CookieOptions { MaxAge = TimeSpan.Zero, Path = "/", HttpOnly = true });
        return Empty();
    }

    /// <summary>招待トークンを確認します。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>招待情報のJSON</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<IResult> InvitationAsync(string token, CancellationToken ct)
    {
        var v = await invitationUC.FindByTokenAsync(token, ct);
        return Results.Json(InvitationJson(v));
    }

    /// <summary>招待レスポンス JSON を組み立てます。</summary>
    /// <param name="v">招待</param>
    /// <returns>JSON化用の辞書</returns>
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
        /// <summary>staff_id クッキーを付与してリダイレクトレスポンスを書き込みます。</summary>
        /// <param name="ctx">HTTPコンテキスト</param>
        /// <returns>完了済みタスク</returns>
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
