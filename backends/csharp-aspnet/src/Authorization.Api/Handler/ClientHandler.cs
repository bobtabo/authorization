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
    /// <param name="name">名称</param>
    /// <param name="postCode">郵便番号</param>
    /// <param name="pref">都道府県</param>
    /// <param name="city">市区町村</param>
    /// <param name="address">番地</param>
    /// <param name="building">建物名（任意）</param>
    /// <param name="tel">電話番号</param>
    /// <param name="email">メールアドレス</param>
    /// <returns>全項目が妥当な場合は true</returns>
    public static bool ValidateStore(string name, string postCode, string pref, string city, string address,
        string? building, string tel, string email) =>
        Required(name, 255) && Required(postCode, 8) && Required(pref, 50) && Required(city, 100) &&
        Required(address, 255) && Optional(building, 255) &&
        tel.Length > 0 && TelPattern().IsMatch(tel) &&
        email.Length > 0 && email.Length <= 255 && EmailPattern().IsMatch(email);

    /// <summary>更新ボディを検証します（存在する項目のみ）。</summary>
    /// <param name="name">名称（未指定なら検証しない）</param>
    /// <param name="postCode">郵便番号（未指定なら検証しない）</param>
    /// <param name="pref">都道府県（未指定なら検証しない）</param>
    /// <param name="city">市区町村（未指定なら検証しない）</param>
    /// <param name="address">番地（未指定なら検証しない）</param>
    /// <param name="building">建物名（未指定なら検証しない）</param>
    /// <param name="tel">電話番号（未指定なら検証しない）</param>
    /// <param name="email">メールアドレス（未指定なら検証しない）</param>
    /// <returns>指定された項目が全て妥当な場合は true</returns>
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
    /// <summary>クライアント一覧を返します。</summary>
    /// <param name="req">HTTPリクエスト（keyword/start_from/start_to/statuses/limit/page/sort/sort_typeを使用）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>一覧データとページャー情報のJSON</returns>
    public async Task<IResult> IndexAsync(HttpRequest req, CancellationToken ct)
    {
        var limit = Math.Max(1, QueryInt(req, "limit") ?? 10);
        var page  = Math.Max(1, QueryInt(req, "page") ?? 1);
        if (SafeOffset(limit, page) is not int offset) return Error(400, "page_out_of_range");
        var statuses = req.Query["statuses"].Concat(req.Query["statuses[]"])
            .SelectMany(v => (v ?? "").Split(','))
            .Select(s => int.TryParse(s.Trim(), out var r) ? r : (int?)null)
            .OfType<int>()
            .ToList();

        var dto = new ClientListConditionDto(
            Keyword:   Query(req, "keyword"),
            StartFrom: Query(req, "start_from"),
            StartTo:   Query(req, "start_to"),
            Statuses:  statuses,
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

    /// <summary>クライアント詳細を返します。</summary>
    /// <param name="id">クライアントID（文字列）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クライアント詳細のJSON、IDが不正な場合は400</returns>
    public async Task<IResult> ShowAsync(string id, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var c = await clientUC.FindByIdAsync(clientId, ct);
        return Results.Json(DetailJson(c));
    }

    /// <summary>
    /// クライアントを登録します。登録後、全スタッフへ通知を配信し、利用開始案内メールを送信します。
    /// </summary>
    /// <param name="req">HTTPリクエストボディ（name/post_code/pref/city/address/building/tel/email）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>登録したクライアントIDのJSON（201）、検証エラーの場合は422</returns>
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

    /// <summary>クライアントを更新します（楽観排他ロック）。</summary>
    /// <param name="id">クライアントID（文字列）</param>
    /// <param name="req">HTTPリクエストボディ（更新するフィールドとversion）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>更新後のクライアント詳細のJSON、IDが不正または検証エラーの場合は400/422</returns>
    /// <exception cref="AppException">存在しない場合（404）、バージョン不一致（409）</exception>
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
        var version  = body.Int("version");

        if (!ClientValidation.ValidateUpdate(name, postCode, pref, city, address, building, tel, email))
        {
            return Error(422, "validation_error");
        }
        if (version is not int v) return Error(400, "version_required");

        var dto = new ClientUpdateDto(clientId, name, postCode, pref, city, address, building, tel, email, status,
            executorId, v);
        var c = await clientUC.UpdateAsync(dto, ct);
        return Results.Json(DetailJson(c));
    }

    /// <summary>QRコード用データを返します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>識別子とディープリンクURLのJSON</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<IResult> QrAsync(string identifier, CancellationToken ct)
    {
        var vo = await clientUC.GetQrAsync(new ClientQrDto(identifier), ct);
        return Results.Json(new Dictionary<string, object?>
        {
            ["identifier"]   = vo.Identifier,
            ["deeplink_url"] = vo.DeeplinkUrl,
        });
    }

    /// <summary>スマホアプリ向けクライアント情報を返します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>識別子・名称・状態のJSON</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
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

    /// <summary>利用開始し、アクセストークンを返します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>アクセストークンのJSON</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<IResult> StartAsync(string identifier, CancellationToken ct)
    {
        var vo = await clientUC.StartAsync(new ClientStartDto(identifier), ct);
        return Results.Json(new Dictionary<string, object?> { ["access_token"] = vo.AccessToken });
    }

    /// <summary>利用停止します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>空レスポンス</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<IResult> StopAsync(string identifier, CancellationToken ct)
    {
        await clientUC.StopAsync(new ClientStopDto(identifier), ct);
        return Empty();
    }

    /// <summary>クライアントを論理削除します。</summary>
    /// <param name="id">クライアントID（文字列）</param>
    /// <param name="req">HTTPリクエストボディ（version）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>空レスポンス、IDが不正な場合は400</returns>
    /// <exception cref="AppException">バージョン未指定（400）、存在しない場合（404）、バージョン不一致（409）</exception>
    public async Task<IResult> DestroyAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var body = await ReadJsonObjectAsync(req, ct);
        await clientUC.DestroyAsync(clientId, StaffId(req), body.Int("version"), ct);
        return Empty();
    }

    /// <summary>クライアントのJWT発行履歴一覧を返します。</summary>
    /// <param name="id">クライアントID（文字列）</param>
    /// <param name="req">HTTPリクエスト（limit/page/sort/sort_typeを使用）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>履歴一覧とページャー情報のJSON、IDが不正な場合は400</returns>
    public async Task<IResult> JwtHistoriesAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var clientId)) return InvalidId();
        var limit = Math.Max(1, QueryInt(req, "limit") ?? 10);
        var page  = Math.Max(1, QueryInt(req, "page") ?? 1);
        if (SafeOffset(limit, page) is not int offset) return Error(400, "page_out_of_range");

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

    /// <summary>クライアント詳細を JSON 用の辞書に変換します。</summary>
    /// <param name="c">クライアント詳細</param>
    /// <returns>JSON化用の辞書</returns>
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
