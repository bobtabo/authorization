using Authorization.Api.Domain.Notification;

namespace Authorization.Api.Tests.UseCase.Notification;

public sealed class FakeNotificationRepository : INotificationRepository
{
    public readonly List<(long StaffId, int MessageType, string Title, string Message, long CreatedBy, string? Url)> Stored = [];
    public Func<long, Task>? OnStore;
    public Action<int>? OnListPage;
    public bool MarkReadResult = true;
    public long? LastMarkReadId;

    public Task<NotificationPage> ListPageAsync(long staffId, string? cursor, int limit, CancellationToken ct = default)
    {
        OnListPage?.Invoke(limit);
        return Task.FromResult(new NotificationPage([], null));
    }

    public Task<NotificationCountsVo> CountsAsync(long staffId, CancellationToken ct = default) =>
        Task.FromResult(new NotificationCountsVo(0, 0));

    public Task<long> BulkMarkReadAsync(long staffId, IReadOnlyList<long> ids, bool all, CancellationToken ct = default) =>
        Task.FromResult(0L);

    public async Task StoreAsync(long staffId, int messageType, string title, string message, long createdBy, string? url, CancellationToken ct = default)
    {
        if (OnStore is not null) await OnStore(staffId);
        Stored.Add((staffId, messageType, title, message, createdBy, url));
    }

    public Task<bool> MarkReadAsync(long id, CancellationToken ct = default)
    {
        LastMarkReadId = id;
        return Task.FromResult(MarkReadResult);
    }
}
