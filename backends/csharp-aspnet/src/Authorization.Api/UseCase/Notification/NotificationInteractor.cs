/*
 * 通知ユースケースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Notification;
using Authorization.Api.Domain.Staff;

namespace Authorization.Api.UseCase.Notification;

/// <summary>全スタッフ配信 DTO です。</summary>
public sealed record NotificationFanOutDto(int MessageType, string Title, string Message, long ExecutorId, string Url = "");

/// <summary>通知ユースケースです。</summary>
public sealed class NotificationInteractor(
    INotificationRepository repo,
    IStaffRepository staffRepo,
    ILogger<NotificationInteractor>? logger = null)
{
    /// <summary>cursor ページネーションで通知一覧を返します。limit は 1〜100 に丸めます。</summary>
    public Task<NotificationPage> ListPageAsync(long staffId, string? cursor, int limit, CancellationToken ct = default) =>
        repo.ListPageAsync(staffId, cursor, Math.Clamp(limit, 1, 100), ct);

    /// <summary>未読・総件数を返します。</summary>
    public Task<NotificationCountsVo> CountsAsync(long staffId, CancellationToken ct = default) =>
        repo.CountsAsync(staffId, ct);

    /// <summary>全件既読にします。</summary>
    public Task BulkMarkReadAsync(long staffId, CancellationToken ct = default) =>
        repo.BulkMarkReadAsync(staffId, [], true, ct);

    /// <summary>有効な全スタッフへ通知を配信します。個別の失敗は無視します。</summary>
    public async Task FanOutAsync(NotificationFanOutDto dto, CancellationToken ct = default)
    {
        var staffs = await staffRepo.FindAllActiveAsync(ct);
        var url    = string.IsNullOrWhiteSpace(dto.Url) ? null : dto.Url;
        foreach (var s in staffs)
        {
            try
            {
                await repo.StoreAsync(s.Id, dto.MessageType, dto.Title, dto.Message, dto.ExecutorId, url, ct);
            }
            catch (Exception e)
            {
                logger?.LogWarning(e, "notification fan-out failed: staff_id={StaffId}", s.Id);
            }
        }
    }

    /// <summary>1 件を既読にします。</summary>
    public Task<bool> MarkReadAsync(long id, CancellationToken ct = default) => repo.MarkReadAsync(id, ct);
}
