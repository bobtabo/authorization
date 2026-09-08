/*
 * 招待（管理者）・Gate・通知ハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Config;
using Authorization.Api.Domain.Notification;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Gate;
using Authorization.Api.UseCase.Invitation;
using Authorization.Api.UseCase.Notification;
using static Authorization.Api.Handler.HttpHelpers;

namespace Authorization.Api.Handler;

/// <summary>管理者向け招待ハンドラーです。</summary>
public sealed class AdminInvitationHandler(InvitationInteractor invitationUC)
{
    /// <summary>指定ロールの現在の招待を返します。</summary>
    /// <param name="req">HTTPリクエスト（roleクエリ。未指定はメンバー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>招待情報のJSON、roleが不正な場合は400</returns>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<IResult> IndexAsync(HttpRequest req, CancellationToken ct)
    {
        var (role, error) = ResolveRole(req);
        if (error is not null) return error;
        var v = await invitationUC.CurrentAsync(role, ct);
        return Results.Json(AuthHandler.InvitationJson(v));
    }

    /// <summary>指定ロールの招待を新規発行します。</summary>
    /// <param name="req">HTTPリクエスト（roleクエリ。未指定はメンバー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>招待情報のJSON、未認証の場合は401、roleが不正な場合は400</returns>
    public async Task<IResult> IssueAsync(HttpRequest req, CancellationToken ct)
    {
        if (StaffId(req) == 0) return Unauthenticated();
        var (role, error) = ResolveRole(req);
        if (error is not null) return error;
        var v = await invitationUC.IssueAsync(role, ct);
        return Results.Json(AuthHandler.InvitationJson(v));
    }

    /// <summary>roleクエリパラメータを検証し、ロール値またはエラー結果を返します。</summary>
    /// <param name="req">HTTPリクエスト（roleクエリ）</param>
    /// <returns>ロール値（未指定はメンバー）とエラー結果（無ければnull）のタプル</returns>
    private static (int Role, IResult? Error) ResolveRole(HttpRequest req)
    {
        var raw = Query(req, "role");
        if (raw is null) return (StaffRole.Member, null);
        if (!int.TryParse(raw, out var role) || !StaffRole.IsValid(role)) return (0, Error(400, "invalid_role"));
        return (role, null);
    }
}

/// <summary>Gate（JWT 発行・検証）ハンドラーです。</summary>
public sealed class GateHandler(GateInteractor gateUC)
{
    /// <summary>クライアントのアクセストークン（Bearer）とメンバーIDでJWTを発行します。</summary>
    /// <param name="req">HTTPリクエスト（memberクエリ、Authorizationヘッダー）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>JWTのJSON、memberが未指定なら400、Bearerトークンが無ければ401</returns>
    /// <exception cref="AppException">クライアントが存在しない場合（404）</exception>
    public async Task<IResult> IssueAsync(HttpRequest req, CancellationToken ct)
    {
        var member = Query(req, "member");
        if (string.IsNullOrWhiteSpace(member)) return Error(400, "member_required");

        var auth        = req.Headers.Authorization.ToString();
        var accessToken = auth.StartsWith("Bearer ", StringComparison.Ordinal) ? auth["Bearer ".Length..] : "";
        if (string.IsNullOrEmpty(accessToken)) return Error(401, "client_not_found");

        var vo = await gateUC.IssueTokenAsync(new GateIssueDto(accessToken, member), ct);
        return Results.Json(new Dictionary<string, object?> { ["token"] = vo.Token });
    }

    /// <summary>クライアントの公開鍵でJWTを検証し、クレームを返します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="req">HTTPリクエスト（tokenクエリ）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クレームのJSON、tokenが未指定なら400</returns>
    /// <exception cref="AppException">クライアントが存在しない場合（404）、検証失敗（401）</exception>
    public async Task<IResult> VerifyAsync(string identifier, HttpRequest req, CancellationToken ct)
    {
        var token = Query(req, "token");
        if (string.IsNullOrWhiteSpace(token)) return Error(400, "token_required");

        var vo = await gateUC.VerifyAsync(new GateVerifyDto(identifier, token), ct);
        return Results.Json(vo.Claims.ToDictionary(kv => kv.Key, kv => kv.Value));
    }
}

/// <summary>通知ハンドラーです。</summary>
public sealed class NotificationHandler(NotificationInteractor notificationUC, AppSettings app)
{
    /// <summary>未読・総件数を返します。</summary>
    /// <param name="req">HTTPリクエスト（staff_idをクッキーから取得）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>未読件数・総件数のJSON、未認証の場合は401</returns>
    public async Task<IResult> CountsAsync(HttpRequest req, CancellationToken ct)
    {
        var staffId = StaffId(req);
        if (staffId == 0) return Unauthenticated();
        var vo = await notificationUC.CountsAsync(staffId, ct);
        return Results.Json(new Dictionary<string, object?> { ["unread"] = vo.Unread, ["total"] = vo.Total });
    }

    /// <summary>通知一覧をcursorページネーションで返します。</summary>
    /// <param name="req">HTTPリクエスト（cursor/limitクエリ、staff_idをクッキーから取得）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>通知一覧と次カーソルのJSON、未認証の場合は401</returns>
    public async Task<IResult> IndexAsync(HttpRequest req, CancellationToken ct)
    {
        var staffId = StaffId(req);
        if (staffId == 0) return Unauthenticated();

        var limit = QueryInt(req, "limit") ?? app.NotificationDefaultLimit;
        var page  = await notificationUC.ListPageAsync(staffId, Query(req, "cursor"), limit, ct);

        return Results.Json(new Dictionary<string, object?>
        {
            ["items"]       = page.Items.Select(ToJson).ToList(),
            ["next_cursor"] = page.NextCursor,
        });
    }

    /// <summary>全件既読にします。</summary>
    /// <param name="req">HTTPリクエスト（staff_idをクッキーから取得）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>空レスポンス、未認証の場合は401</returns>
    public async Task<IResult> ReadAllAsync(HttpRequest req, CancellationToken ct)
    {
        var staffId = StaffId(req);
        if (staffId == 0) return Unauthenticated();
        await notificationUC.BulkMarkReadAsync(staffId, ct);
        return Empty();
    }

    /// <summary>1 件を既読にします。</summary>
    /// <param name="id">通知ID（文字列）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>更新した通知IDのJSON、IDが不正な場合は400</returns>
    public async Task<IResult> ReadAsync(string id, CancellationToken ct)
    {
        if (!long.TryParse(id, out var notificationId)) return InvalidId();
        await notificationUC.MarkReadAsync(notificationId, ct);
        return Results.Json(new Dictionary<string, object?> { ["id"] = notificationId });
    }

    /// <summary>通知 1 件を JSON に変換します。</summary>
    /// <param name="n">通知</param>
    /// <returns>JSON化用の辞書</returns>
    public static Dictionary<string, object?> ToJson(Notification n) => new()
    {
        ["id"]           = n.Id,
        ["staff_id"]     = n.StaffId,
        ["message_type"] = n.MessageType,
        ["title"]        = n.Title,
        ["message"]      = n.Message,
        ["url"]          = n.Url,
        ["read"]         = n.Read,
        ["created_at"]   = DateFormat.ToMinute(n.CreatedAt),
        ["updated_at"]   = DateFormat.ToMinute(n.UpdatedAt),
    };
}
