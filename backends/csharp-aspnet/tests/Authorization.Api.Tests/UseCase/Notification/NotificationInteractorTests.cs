using Authorization.Api.Tests.UseCase.Staff;
using Authorization.Api.UseCase.Notification;

namespace Authorization.Api.Tests.UseCase.Notification;

public class NotificationInteractorTests
{
    private static Authorization.Api.Domain.Staff.Staff MakeStaff(long id, DateTime? deletedAt = null) => new()
    {
        Id        = id,
        Name      = $"Staff {id}",
        Email     = $"staff{id}@example.com",
        DeletedAt = deletedAt,
    };

    [Fact]
    public async Task FanOutAsync_SendsOnlyToActiveStaff()
    {
        var staffRepo = new FakeStaffRepository()
            .Add(MakeStaff(1))
            .Add(MakeStaff(2, deletedAt: DateTime.Now));
        var notifRepo = new FakeNotificationRepository();
        var uc = new NotificationInteractor(notifRepo, staffRepo);

        await uc.FanOutAsync(new NotificationFanOutDto(1, "Title", "Message", ExecutorId: 9));

        Assert.Single(notifRepo.Stored);
        Assert.Equal(1, notifRepo.Stored[0].StaffId);
    }

    [Fact]
    public async Task FanOutAsync_BlankUrl_StoredAsNull()
    {
        var staffRepo = new FakeStaffRepository().Add(MakeStaff(1));
        var notifRepo = new FakeNotificationRepository();
        var uc = new NotificationInteractor(notifRepo, staffRepo);

        await uc.FanOutAsync(new NotificationFanOutDto(1, "Title", "Message", ExecutorId: 9, Url: "  "));

        Assert.Null(notifRepo.Stored[0].Url);
    }

    [Fact]
    public async Task FanOutAsync_IndividualFailure_DoesNotStopRemainingSends()
    {
        var staffRepo = new FakeStaffRepository().Add(MakeStaff(1)).Add(MakeStaff(2)).Add(MakeStaff(3));
        var notifRepo = new FakeNotificationRepository
        {
            OnStore = staffId => staffId == 2 ? throw new InvalidOperationException("boom") : Task.CompletedTask,
        };
        var uc = new NotificationInteractor(notifRepo, staffRepo);

        await uc.FanOutAsync(new NotificationFanOutDto(1, "Title", "Message", ExecutorId: 9));

        Assert.Equal(2, notifRepo.Stored.Count);
        Assert.Contains(notifRepo.Stored, s => s.StaffId == 1);
        Assert.Contains(notifRepo.Stored, s => s.StaffId == 3);
    }

    [Theory]
    [InlineData(0, 1)]
    [InlineData(1000, 100)]
    [InlineData(50, 50)]
    public async Task ListPageAsync_ClampsLimitTo1To100(int requested, int expectedClamped)
    {
        int? seenLimit = null;
        var notifRepo = new FakeNotificationRepository { OnListPage = l => seenLimit = l };
        var uc = new NotificationInteractor(notifRepo, new FakeStaffRepository());

        await uc.ListPageAsync(1, null, requested);

        Assert.Equal(expectedClamped, seenLimit);
    }

    [Fact]
    public async Task MarkReadAsync_DelegatesToRepository()
    {
        var notifRepo = new FakeNotificationRepository { MarkReadResult = true };
        var uc = new NotificationInteractor(notifRepo, new FakeStaffRepository());

        var result = await uc.MarkReadAsync(42);

        Assert.True(result);
        Assert.Equal(42, notifRepo.LastMarkReadId);
    }
}
