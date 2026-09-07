/*
 * スタッフハンドラーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Staff;
using static Authorization.Api.Handler.HttpHelpers;

namespace Authorization.Api.Handler;

/// <summary>スタッフハンドラーです。</summary>
public sealed class StaffHandler(StaffInteractor staffUC)
{
    public async Task<IResult> IndexAsync(HttpRequest req, CancellationToken ct)
    {
        var roles = req.Query["roles"]
            .SelectMany(v => (v ?? "").Split(','))
            .Select(s => int.TryParse(s.Trim(), out var r) ? r : (int?)null)
            .OfType<int>()
            .ToList();
        var limit  = Math.Max(1, QueryInt(req, "limit") ?? 10);
        var page   = Math.Max(1, QueryInt(req, "page") ?? 1);
        var offset = limit * (page - 1);

        var cond = new StaffCondition
        {
            Keyword  = Query(req, "keyword"),
            Roles    = roles,
            Offset   = offset,
            Limit    = limit,
            Sort     = Query(req, "sort"),
            SortType = Query(req, "sort_type"),
        };

        var (staffs, count) = await staffUC.FindByConditionWithCountAsync(cond, ct);
        var data = staffs.Select(s => new Dictionary<string, object?>
        {
            ["id"]         = s.Id,
            ["name"]       = s.Name,
            ["email"]      = s.Email,
            ["role"]       = s.Role,
            ["status"]     = s.Status,
            ["created_at"] = DateFormat.ToMinute(s.CreatedAt),
            ["updated_at"] = DateFormat.ToMinute(s.UpdatedAt),
            ["version"]    = s.Version,
        }).ToList();

        return Results.Json(new Dictionary<string, object?>
        {
            ["data"]  = data,
            ["pager"] = Pager.Build(count, limit, offset, staffs.Count).ToJson(),
        });
    }

    public async Task<IResult> UpdateRoleAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var staffId)) return InvalidId();
        var body = await ReadJsonObjectAsync(req, ct);
        var role = body.Int("role");
        if (role is null) return Error(400, "role_required");

        await staffUC.UpdateRoleAsync(new StaffUpdateRoleDto(staffId, role.Value, StaffId(req)), ct);
        return Results.Json(new Dictionary<string, object?> { ["id"] = staffId });
    }

    public async Task<IResult> RestoreAsync(string id, CancellationToken ct)
    {
        if (!long.TryParse(id, out var staffId)) return InvalidId();
        await staffUC.RestoreAsync(staffId, ct);
        return Results.Json(new Dictionary<string, object?> { ["id"] = staffId });
    }

    public async Task<IResult> DestroyAsync(string id, HttpRequest req, CancellationToken ct)
    {
        if (!long.TryParse(id, out var staffId)) return InvalidId();
        await staffUC.DestroyAsync(new StaffDestroyDto(staffId, StaffId(req)), ct);
        return Results.Json(new Dictionary<string, object?> { ["id"] = staffId });
    }
}
