/*
 * クライアントハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Text.RegularExpressions;
using Authorization.Api.Config;
using Authorization.Api.Domain.Client;
using Authorization.Api.Domain.Notification;
using Authorization.Api.Infrastructure.Mail;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Client;
using Authorization.Api.UseCase.Notification;
using static Authorization.Api.Handler.HttpHelpers;

namespace Authorization.Api.Handler;

/// <summary>クライアント登録/更新ボディの検証です。</summary>
public static partial class ClientValidation
{
    [GeneratedRegex(@"^\d{10,11}$")]
    private static partial Regex TelPattern();

    [GeneratedRegex(@"^[^\s@]+@[^\s@]+\.[^\s@]+$")]
    private static partial Regex EmailPattern();

    /// <summary>登録ボディを検証します。</summary>
    public static bool ValidateStore(string name, string postCode, string pref, string city, string address,
        string? building, string tel, string email) =>
        Required(name, 255) && Required(postCode, 8) && Required(pref, 50) && Required(city, 100) &&
        Required(address, 255) && Optional(building, 255) &&
        tel.Length > 0 && TelPattern().IsMatch(tel) &&
        email.Length > 0 && email.Length <= 255 && EmailPattern().IsMatch(email);

    /// <summary>更新ボディを検証します（存在する項目のみ）。</summary>
    public static bool ValidateUpdate(string? name, string? postCode, string? pref, string? city, string? address,
        string? building, string? tel, string? email) =>
        Optional(name, 255) && Optional(postCode, 8) && Optional(pref, 50) && Optional(city, 100) &&
        Optional(address, 255) && Optional(building, 255) &&
        (tel is null || TelPattern().IsMatch(tel)) &&
        (email is null || (email.Length <= 255 && EmailPattern().IsMatch(email)));

    private static bool Required(string v, int max) => v.Length > 0 && v.Length <= max;
    private static bool Optional(string? v, int max) => v is null || v.Length <= max;
}

/// <summary>クライアントハンドラーです。</summary>
public sealed class ClientHandler(
    ClientInteractor clientUC,
    NotificationInteractor notificationUC,
    IMailer mailer,
    IJwtHistoryRepository jwtHistoryRepo,
    AppSettings app)
{
    public async Task<IResult> IndexAsync(HttpRequest req, CancellationToken ct)
    {
        var limit  = Math.Max(1, QueryInt(req, "limit") ?? 10);
        var page   = Math.Max(1, QueryInt(req, "page") ?? 1);
        var offset = limit * (page - 1);

        var dto = new ClientListConditionDto(
            Keyword:   Query(req, "keyword"),
            StartFrom: Query(req, "start_from"),
            StartTo:   Query(req, "start_to"),
            Offset:    offset,
            Limit:     limit,
            Sort:      Query(req, "sort"),
            SortType:  Query(req, "sort_type"));

        var (items, count) = await clientUC.FindByConditionWithCountAsync(dto, ct);
        var data = items.Select(c => new Dictionary<string, object?>
        {
            ["id"]         = c.Id,
            ["name"]       = c.Name,
            ["status"]     = c.Status,
            ["start_at"]   = DateFormat.ToMinute(c.StartAt),
            ["stop_at"]    = DateFormat.ToMinute(c.StopAt),
            ["created_at"] = DateFormat.ToMinute(c.CreatedAt),
            ["updated_at"] = DateFormat.ToMinute(c.UpdatedAt),
        }).ToList();

        return Results.Json(new Dictionary<string, object?>
        {
            ["data"]  = data,
            ["pager"] = Pager.Build(count, limit, offset, items.Count).ToJson(),
        });
    }

    public async Task<IResult> ShowAsync(string id, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var c = await clientUC.FindByIdAsync(clientId, ct);
        return Results.Json(DetailJson(c));
    }

    public async Task<IResult> StoreAsync(HttpRequest req, CancellationToken ct)
    {
        var executorId = StaffId(req);
        var body = await ReadJsonObjectAsync(req, ct);

        var name     = body.Str("name") ?? "";
        var postCode = body.Str("post_code") ?? "";
        var pref     = body.Str("pref") ?? "";
        var city     = body.Str("city") ?? "";
        var address  = body.Str("address") ?? "";
        var building = body.Str("building");
        var tel      = body.Str("tel") ?? "";
        var email    = body.Str("email") ?? "";

        if (!ClientValidation.ValidateStore(name, postCode, pref, city, address, building, tel, email))
        {
            return Error(422, "validation_error");
        }

        var client = await clientUC.StoreAsync(new ClientStoreDto(
            name, postCode, pref, city, address, building ?? "", tel, email, executorId), ct);

        await notificationUC.FanOutAsync(new NotificationFanOutDto(
            MessageType: NotificationMessageType.Info,
            Title:       "新しいクライアントが登録されました",
            Message:     client.Name,
            ExecutorId:  executorId,
            Url:         $"/clients/show?id={client.Id}"), ct);

        var activateUrl = $"{app.FrontendUrl}/clients/{client.Identifier}/qr";
        _ = mailer.SendActivationAsync(client.Email, client.Name, activateUrl, CancellationToken.None);

        return Results.Json(new Dictionary<string, object?> { ["id"] = client.Id }, statusCode: 201);
    }

    public async Task<IResult> UpdateAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var executorId = StaffId(req);
        var body = await ReadJsonObjectAsync(req, ct);

        var name     = body.Str("name");
        var postCode = body.Str("post_code");
        var pref     = body.Str("pref");
        var city     = body.Str("city");
        var address  = body.Str("address");
        var building = body.Str("building");
        var tel      = body.Str("tel");
        var email    = body.Str("email");
        var status   = body.Int("status");

        if (!ClientValidation.ValidateUpdate(name, postCode, pref, city, address, building, tel, email))
        {
            return Error(422, "validation_error");
        }

        var dto = new ClientUpdateDto(clientId, name, postCode, pref, city, address, building, tel, email, status,
            executorId, body.Int("version") ?? 0);
        var c = await clientUC.UpdateAsync(dto, ct);
        return Results.Json(DetailJson(c));
    }

    public async Task<IResult> QrAsync(string identifier, CancellationToken ct)
    {
        var vo = await clientUC.GetQrAsync(new ClientQrDto(identifier), ct);
        return Results.Json(new Dictionary<string, object?>
        {
            ["identifier"]   = vo.Identifier,
            ["deeplink_url"] = vo.DeeplinkUrl,
        });
    }

    public async Task<IResult> InfoAsync(string identifier, CancellationToken ct)
    {
        var vo = await clientUC.GetInfoAsync(new ClientInfoDto(identifier), ct);
        return Results.Json(new Dictionary<string, object?>
        {
            ["identifier"] = vo.Identifier,
            ["name"]       = vo.Name,
            ["status"]     = vo.Status,
        });
    }

    public async Task<IResult> StartAsync(string identifier, CancellationToken ct)
    {
        var vo = await clientUC.StartAsync(new ClientStartDto(identifier), ct);
        return Results.Json(new Dictionary<string, object?> { ["access_token"] = vo.AccessToken });
    }

    public async Task<IResult> StopAsync(string identifier, CancellationToken ct)
    {
        await clientUC.StopAsync(new ClientStopDto(identifier), ct);
        return Empty();
    }

    public async Task<IResult> DestroyAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var body = await ReadJsonObjectAsync(req, ct);
        await clientUC.DestroyAsync(clientId, StaffId(req), body.Int("version"), ct);
        return Empty();
    }

    public async Task<IResult> JwtHistoriesAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var limit  = Math.Max(1, QueryInt(req, "limit") ?? 10);
        var page   = Math.Max(1, QueryInt(req, "page") ?? 1);
        var offset = limit * (page - 1);

        var cond = new JwtHistoryCondition(clientId, offset, limit,
            Query(req, "sort") ?? "issue_at", Query(req, "sort_type") ?? "desc");

        var count     = await jwtHistoryRepo.CountByConditionAsync(cond, ct);
        var histories = await jwtHistoryRepo.FindByConditionAsync(cond, ct);
        var data = histories.Select(h => new Dictionary<string, object?>
        {
            ["id"]        = h.Id,
            ["member_id"] = h.MemberId,
            ["issue_at"]  = DateFormat.ToSecond(h.IssueAt),
            ["jwt"]       = h.Jwt,
        }).ToList();

        return Results.Json(new Dictionary<string, object?>
        {
            ["data"]  = data,
            ["pager"] = Pager.Build(count, limit, offset, histories.Count).ToJson(),
        });
    }

    private static Dictionary<string, object?> DetailJson(ClientDetailVo c) => new()
    {
        ["id"]         = c.Id,
        ["name"]       = c.Name,
        ["identifier"] = c.Identifier,
        ["post_code"]  = c.PostCode,
        ["pref"]       = c.Pref,
        ["city"]       = c.City,
        ["address"]    = c.Address,
        ["building"]   = c.Building,
        ["tel"]        = c.Tel,
        ["email"]      = c.Email,
        ["status"]     = c.Status,
        ["start_at"]   = DateFormat.ToMinute(c.StartAt),
        ["stop_at"]    = DateFormat.ToMinute(c.StopAt),
        ["created_at"] = DateFormat.ToMinute(c.CreatedAt),
        ["updated_at"] = DateFormat.ToMinute(c.UpdatedAt),
        ["version"]    = c.Version,
    };
}
