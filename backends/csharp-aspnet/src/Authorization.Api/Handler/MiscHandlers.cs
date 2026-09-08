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
    public async Task<IResult> IndexAsync(HttpRequest req, CancellationToken ct)
    {
        var (role, error) = ResolveRole(req);
        if (error is not null) return error;
        var v = await invitationUC.CurrentAsync(role, ct);
        return Results.Json(AuthHandler.InvitationJson(v));
    }

    public async Task<IResult> IssueAsync(HttpRequest req, CancellationToken ct)
    {
        if (StaffId(req) == 0) return Unauthenticated();
        var (role, error) = ResolveRole(req);
        if (error is not null) return error;
        var v = await invitationUC.IssueAsync(role, ct);
        return Results.Json(AuthHandler.InvitationJson(v));
    }

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
    public async Task<IResult> CountsAsync(HttpRequest req, CancellationToken ct)
    {
        var staffId = StaffId(req);
        if (staffId == 0) return Unauthenticated();
        var vo = await notificationUC.CountsAsync(staffId, ct);
        return Results.Json(new Dictionary<string, object?> { ["unread"] = vo.Unread, ["total"] = vo.Total });
    }

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

    public async Task<IResult> ReadAllAsync(HttpRequest req, CancellationToken ct)
    {
        var staffId = StaffId(req);
        if (staffId == 0) return Unauthenticated();
        await notificationUC.BulkMarkReadAsync(staffId, ct);
        return Empty();
    }

    public async Task<IResult> ReadAsync(string id, CancellationToken ct)
    {
        if (!long.TryParse(id, out var notificationId)) return InvalidId();
        await notificationUC.MarkReadAsync(notificationId, ct);
        return Results.Json(new Dictionary<string, object?> { ["id"] = notificationId });
    }

    /// <summary>通知 1 件を JSON に変換します。</summary>
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
