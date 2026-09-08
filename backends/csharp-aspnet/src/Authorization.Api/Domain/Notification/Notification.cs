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
    /// <summary>cursorページネーションで通知一覧を返します。</summary>
    /// <param name="staffId">スタッフID</param>
    /// <param name="cursor">前回レスポンスの next_cursor（先頭ページは null）</param>
    /// <param name="limit">取得件数</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>通知一覧と次カーソル</returns>
    Task<NotificationPage> ListPageAsync(long staffId, string? cursor, int limit, CancellationToken ct = default);

    /// <summary>未読・総件数を返します。</summary>
    /// <param name="staffId">スタッフID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>未読件数と総件数</returns>
    Task<NotificationCountsVo> CountsAsync(long staffId, CancellationToken ct = default);

    /// <summary>複数件を既読にします。</summary>
    /// <param name="staffId">スタッフID</param>
    /// <param name="ids">対象通知ID一覧（allがfalseの場合のみ使用）</param>
    /// <param name="all">trueなら全件、falseならidsで指定した件のみ</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>更新件数</returns>
    Task<long> BulkMarkReadAsync(long staffId, IReadOnlyList<long> ids, bool all, CancellationToken ct = default);

    /// <summary>通知を1件保存します。</summary>
    /// <param name="staffId">宛先スタッフID</param>
    /// <param name="messageType">メッセージ種別</param>
    /// <param name="title">タイトル</param>
    /// <param name="message">本文</param>
    /// <param name="createdBy">作成者ID</param>
    /// <param name="url">リンク先URL（任意）</param>
    /// <param name="ct">キャンセレーショントークン</param>
    Task StoreAsync(long staffId, int messageType, string title, string message, long createdBy, string? url, CancellationToken ct = default);

    /// <summary>1 件を既読にします。</summary>
    /// <param name="id">通知ID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>更新できた場合は true</returns>
    Task<bool> MarkReadAsync(long id, CancellationToken ct = default);
}
