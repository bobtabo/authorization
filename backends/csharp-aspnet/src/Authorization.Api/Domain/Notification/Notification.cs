/*
 * 通知 ドメインモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Domain.Notification;

/// <summary>通知メッセージ種別です。</summary>
public static class NotificationMessageType
{
    public const int Info = 1;
    public const int Warn = 2;
    public const int Ok   = 3;
}

/// <summary>通知エンティティです。</summary>
public sealed record Notification(
    long Id,
    long StaffId,
    int MessageType,
    string Title,
    string Message,
    string? Url,
    bool Read,
    DateTime CreatedAt,
    long CreatedBy,
    DateTime UpdatedAt,
    long UpdatedBy,
    DateTime? DeletedAt,
    int Version
);

/// <summary>cursor ページネーションの 1 ページです。</summary>
public sealed record NotificationPage(List<Notification> Items, string? NextCursor);

/// <summary>未読・総件数 VO です。</summary>
public sealed record NotificationCountsVo(long Unread, long Total);

/// <summary>通知リポジトリです。</summary>
public interface INotificationRepository
{
    Task<NotificationPage> ListPageAsync(long staffId, string? cursor, int limit, CancellationToken ct = default);
    Task<NotificationCountsVo> CountsAsync(long staffId, CancellationToken ct = default);
    Task<long> BulkMarkReadAsync(long staffId, IReadOnlyList<long> ids, bool all, CancellationToken ct = default);
    Task StoreAsync(long staffId, int messageType, string title, string message, long createdBy, string? url, CancellationToken ct = default);
    Task<bool> MarkReadAsync(long id, CancellationToken ct = default);
}
