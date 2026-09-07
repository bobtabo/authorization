/*
 * 通知リポジトリ（EF Core）モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Text;
using Authorization.Api.Domain.Notification;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Infrastructure.Model;
using Authorization.Api.Support;
using Microsoft.EntityFrameworkCore;
using NotificationEntity = Authorization.Api.Domain.Notification.Notification;

namespace Authorization.Api.Infrastructure.Persistence;

/// <summary>EF Core による通知リポジトリです。</summary>
public sealed class EfNotificationRepository(AppDbContext db) : INotificationRepository
{
    public async Task<NotificationPage> ListPageAsync(long staffId, string? cursor, int limit, CancellationToken ct = default)
    {
        var q = db.Notifications.AsNoTracking().Where(n => n.StaffId == staffId && n.DeletedAt == null);

        if (cursor is not null)
        {
            var (dt, cursorId) = DecodeCursor(cursor) ?? throw AppException.BadRequest("invalid_cursor");
            q = q.Where(n => n.CreatedAt < dt || (n.CreatedAt == dt && n.Id < cursorId));
        }

        var rows = await q
            .OrderByDescending(n => n.CreatedAt)
            .ThenByDescending(n => n.Id)
            .Take(limit + 1)
            .ToListAsync(ct);

        var hasNext = rows.Count > limit;
        var items   = rows.Take(limit).Select(ToEntity).ToList();
        var next    = hasNext && items.Count > 0 ? EncodeCursor(items[^1].CreatedAt, items[^1].Id) : null;
        return new NotificationPage(items, next);
    }

    public async Task<NotificationCountsVo> CountsAsync(long staffId, CancellationToken ct = default)
    {
        var baseQ  = db.Notifications.AsNoTracking().Where(n => n.StaffId == staffId && n.DeletedAt == null);
        var total  = await baseQ.LongCountAsync(ct);
        var unread = await baseQ.Where(n => !n.Read).LongCountAsync(ct);
        return new NotificationCountsVo(unread, total);
    }

    public async Task<long> BulkMarkReadAsync(long staffId, IReadOnlyList<long> ids, bool all, CancellationToken ct = default)
    {
        var now = DateTime.Now;
        var q   = db.Notifications.Where(n => n.StaffId == staffId && !n.Read && n.DeletedAt == null);
        if (!all)
        {
            if (ids.Count == 0) return 0;
            q = q.Where(n => ids.Contains(n.Id));
        }
        return await q.ExecuteUpdateAsync(u => u
            .SetProperty(n => n.Read, true)
            .SetProperty(n => n.UpdatedAt, now), ct);
    }

    public async Task StoreAsync(long staffId, int messageType, string title, string message, long createdBy, string? url, CancellationToken ct = default)
    {
        var now = DateTime.Now;
        var m = new NotificationModel
        {
            StaffId     = staffId,
            MessageType = messageType,
            Title       = title,
            Message     = message,
            Url         = url,
            Read        = false,
            CreatedAt   = now,
            CreatedBy   = (int)createdBy,
            UpdatedAt   = now,
            UpdatedBy   = (int)createdBy,
            Version     = 1,
        };
        db.Notifications.Add(m);
        await db.SaveChangesAsync(ct);
        db.Entry(m).State = EntityState.Detached;
    }

    public async Task<bool> MarkReadAsync(long id, CancellationToken ct = default)
    {
        var now  = DateTime.Now;
        var rows = await db.Notifications.Where(n => n.Id == id).ExecuteUpdateAsync(u => u
            .SetProperty(n => n.Read, true)
            .SetProperty(n => n.UpdatedAt, now), ct);
        return rows > 0;
    }

    private static NotificationEntity ToEntity(NotificationModel m) => new(
        m.Id, m.StaffId, m.MessageType, m.Title, m.Message, m.Url, m.Read,
        m.CreatedAt, m.CreatedBy, m.UpdatedAt, m.UpdatedBy, m.DeletedAt, m.Version);

    /// <summary>created_at（UTC 秒扱いのエポック）と id を base64 でエンコードします。</summary>
    public static string EncodeCursor(DateTime dt, long id)
    {
        var epoch = new DateTimeOffset(DateTime.SpecifyKind(dt, DateTimeKind.Utc)).ToUnixTimeSeconds();
        return Convert.ToBase64String(Encoding.UTF8.GetBytes($"{epoch},{id}"));
    }

    /// <summary>cursor を (created_at, id) に復号します。不正な場合は null。</summary>
    public static (DateTime, long)? DecodeCursor(string cursor)
    {
        try
        {
            var raw   = Encoding.UTF8.GetString(Convert.FromBase64String(cursor));
            var parts = raw.Split(',', 2);
            if (parts.Length != 2) return null;
            var dt = DateTimeOffset.FromUnixTimeSeconds(long.Parse(parts[0])).UtcDateTime;
            return (DateTime.SpecifyKind(dt, DateTimeKind.Unspecified), long.Parse(parts[1]));
        }
        catch (Exception e) when (e is FormatException or OverflowException)
        {
            return null;
        }
    }
}
