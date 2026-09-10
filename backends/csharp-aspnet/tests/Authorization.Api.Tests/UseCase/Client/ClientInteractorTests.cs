using Authorization.Api.Domain.Client;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Support;
using Authorization.Api.Tests.UseCase.Gate;
using Authorization.Api.UseCase.Client;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using ClientEntity = Authorization.Api.Domain.Client.Client;

namespace Authorization.Api.Tests.UseCase.Client;

public class ClientInteractorTests : IDisposable
{
    private readonly SqliteConnection connection;
    private readonly AppDbContext db;

    public ClientInteractorTests()
    {
        connection = new SqliteConnection("DataSource=:memory:");
        connection.Open();
        var options = new DbContextOptionsBuilder<AppDbContext>().UseSqlite(connection).Options;
        db = new AppDbContext(options);
        db.Database.EnsureCreated();
    }

    public void Dispose()
    {
        db.Dispose();
        connection.Dispose();
    }

    private static ClientUpdateDto MakeUpdateDto(long id, int version, long executorId = 9, string? name = null,
        int? status = null) => new(
        Id: id, Name: name, PostCode: null, Pref: null, City: null, Address: null, Building: null, Tel: null,
        Email: null, Status: status, ExecutorId: executorId, Version: version);

    private static ClientEntity MakeClient(long id = 1, int status = ClientStatus.Inactive, int version = 1,
        DateTime? startAt = null, DateTime? stopAt = null) => new()
    {
        Id          = id,
        Name        = $"Client {id}",
        Identifier  = $"identifier-{id}",
        AccessToken = $"token-{id}",
        Status      = status,
        Version     = version,
        StartAt     = startAt,
        StopAt      = stopAt,
    };

    // ── FindByConditionWithCountAsync ──

    [Fact]
    public async Task FindByConditionWithCountAsync_MapsToListItemAndReturnsCount()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1)).Add(MakeClient(2, status: ClientStatus.Active));
        var uc = new ClientInteractor(repo, db);

        var (items, count) = await uc.FindByConditionWithCountAsync(new ClientListConditionDto());

        Assert.Equal(2, count);
        Assert.Contains(items, i => i.Id == 2 && i.Status == ClientStatus.Active);
    }

    // ── FindByIdAsync ──

    [Fact]
    public async Task FindByIdAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.FindByIdAsync(999));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task FindByIdAsync_Found_ReturnsDetail()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1));
        var uc = new ClientInteractor(repo, db);

        var detail = await uc.FindByIdAsync(1);

        Assert.Equal("identifier-1", detail.Identifier);
    }

    // ── StoreAsync ──

    [Fact]
    public async Task StoreAsync_CreatesInactiveClientWithGeneratedCredentials()
    {
        var repo = new FakeClientRepository();
        var uc = new ClientInteractor(repo, db);

        var result = await uc.StoreAsync(new ClientStoreDto(
            Name: "New Client", PostCode: "100-0001", Pref: "東京都", City: "千代田区",
            Address: "1-1", Building: "", Tel: "03-0000-0000", Email: "new@example.com", ExecutorId: 9));

        Assert.NotEmpty(result.Identifier);
        Assert.NotEmpty(result.AccessToken);
        var saved = await repo.FindByIdAsync(result.Id);
        Assert.Equal(ClientStatus.Inactive, saved!.Status);
        Assert.NotEmpty(saved.PrivateKey);
        Assert.NotEmpty(saved.PublicKey);
    }

    // ── UpdateAsync ──

    [Fact]
    public async Task UpdateAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.UpdateAsync(MakeUpdateDto(id: 999, version: 1)));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task UpdateAsync_VersionMismatch_ThrowsConflict()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, version: 3));
        var uc = new ClientInteractor(repo, db);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.UpdateAsync(MakeUpdateDto(id: 1, version: 1)));

        Assert.Equal(409, ex.StatusCode);
    }

    [Fact]
    public async Task UpdateAsync_PartialFields_OnlyUpdatesProvidedFields()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1));
        var uc = new ClientInteractor(repo, db);

        var detail = await uc.UpdateAsync(MakeUpdateDto(id: 1, version: 1, name: "Renamed"));

        Assert.Equal("Renamed", detail.Name);
        Assert.Equal("identifier-1", detail.Identifier);
    }

    [Fact]
    public async Task UpdateAsync_StatusToActive_SetsStartAt()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Inactive));
        var uc = new ClientInteractor(repo, db);

        var detail = await uc.UpdateAsync(MakeUpdateDto(id: 1, version: 1, status: ClientStatus.Active));

        Assert.Equal(ClientStatus.Active, detail.Status);
        Assert.NotNull(detail.StartAt);
        Assert.Null(detail.StopAt);
    }

    [Fact]
    public async Task UpdateAsync_StatusToSuspended_SetsStopAt()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Active));
        var uc = new ClientInteractor(repo, db);

        var detail = await uc.UpdateAsync(MakeUpdateDto(id: 1, version: 1, status: ClientStatus.Suspended));

        Assert.Equal(ClientStatus.Suspended, detail.Status);
        Assert.NotNull(detail.StopAt);
    }

    // ── DestroyAsync ──

    [Fact]
    public async Task DestroyAsync_VersionMissing_ThrowsBadRequest()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.DestroyAsync(1, executorId: 9, version: null));

        Assert.Equal(400, ex.StatusCode);
    }

    [Fact]
    public async Task DestroyAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.DestroyAsync(999, executorId: 9, version: 1));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task DestroyAsync_VersionMismatch_ThrowsConflict()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, version: 3));
        var uc = new ClientInteractor(repo, db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.DestroyAsync(1, executorId: 9, version: 1));

        Assert.Equal(409, ex.StatusCode);
    }

    [Fact]
    public async Task DestroyAsync_ValidRequest_ClosesAndSoftDeletes()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Active, version: 1));
        var uc = new ClientInteractor(repo, db);

        await uc.DestroyAsync(1, executorId: 9, version: 1);

        var saved = await repo.FindByIdAsync(1);
        Assert.Equal(ClientStatus.Closed, saved!.Status);
    }

    // ── GetQrAsync / GetInfoAsync ──

    [Fact]
    public async Task GetQrAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.GetQrAsync(new ClientQrDto("unknown")));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task GetQrAsync_Found_ReturnsDeeplink()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1));
        var uc = new ClientInteractor(repo, db);

        var vo = await uc.GetQrAsync(new ClientQrDto("identifier-1"));

        Assert.Contains("identifier-1", vo.DeeplinkUrl);
    }

    [Fact]
    public async Task GetInfoAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.GetInfoAsync(new ClientInfoDto("unknown")));

        Assert.Equal(404, ex.StatusCode);
    }

    // ── StartAsync / StopAsync ──

    [Fact]
    public async Task StartAsync_InactiveClient_TransitionsToActive()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Inactive));
        var uc = new ClientInteractor(repo, db);

        var vo = await uc.StartAsync(new ClientStartDto("identifier-1"));

        Assert.Equal("token-1", vo.AccessToken);
        var saved = await repo.FindByIdAsync(1);
        Assert.Equal(ClientStatus.Active, saved!.Status);
    }

    [Fact]
    public async Task StartAsync_AlreadyActive_KeepsStartAtUnchanged()
    {
        var startAt = new DateTime(2026, 1, 1);
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Active, startAt: startAt));
        var uc = new ClientInteractor(repo, db);

        await uc.StartAsync(new ClientStartDto("identifier-1"));

        var saved = await repo.FindByIdAsync(1);
        Assert.Equal(startAt, saved!.StartAt);
    }

    [Fact]
    public async Task StartAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.StartAsync(new ClientStartDto("unknown")));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task StopAsync_ActiveClient_TransitionsToSuspended()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Active));
        var uc = new ClientInteractor(repo, db);

        await uc.StopAsync(new ClientStopDto("identifier-1"));

        var saved = await repo.FindByIdAsync(1);
        Assert.Equal(ClientStatus.Suspended, saved!.Status);
    }

    [Fact]
    public async Task StopAsync_NotActive_NoOp()
    {
        var repo = new FakeClientRepository().Add(MakeClient(1, status: ClientStatus.Inactive));
        var uc = new ClientInteractor(repo, db);

        await uc.StopAsync(new ClientStopDto("identifier-1"));

        var saved = await repo.FindByIdAsync(1);
        Assert.Equal(ClientStatus.Inactive, saved!.Status);
    }

    [Fact]
    public async Task StopAsync_NotFound_ThrowsNotFound()
    {
        var uc = new ClientInteractor(new FakeClientRepository(), db);

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.StopAsync(new ClientStopDto("unknown")));

        Assert.Equal(404, ex.StatusCode);
    }
}
