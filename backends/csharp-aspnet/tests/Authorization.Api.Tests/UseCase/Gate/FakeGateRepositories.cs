using Authorization.Api.Domain.Client;
using Authorization.Api.Domain.Gate;
using ClientEntity = Authorization.Api.Domain.Client.Client;

namespace Authorization.Api.Tests.UseCase.Gate;

public sealed class FakeClientRepository : IClientRepository
{
    private readonly Dictionary<long, ClientEntity> clients = [];

    public FakeClientRepository Add(ClientEntity c)
    {
        clients[c.Id] = c;
        return this;
    }

    public Task<List<ClientEntity>> FindByConditionAsync(ClientCondition cond, CancellationToken ct = default) =>
        Task.FromResult(clients.Values.ToList());

    public Task<int> CountByConditionAsync(ClientCondition cond, CancellationToken ct = default) =>
        Task.FromResult(clients.Count);

    public Task<ClientEntity?> FindByIdAsync(long id, CancellationToken ct = default) =>
        Task.FromResult(clients.TryGetValue(id, out var c) ? c : null);

    public Task<ClientEntity?> FindByAccessTokenAsync(string accessToken, CancellationToken ct = default) =>
        Task.FromResult(clients.Values.FirstOrDefault(c =>
            c.AccessToken == accessToken && c.Status == ClientStatus.Active && c.DeletedAt is null));

    public Task<ClientEntity?> FindByIdentifierAsync(string identifier, CancellationToken ct = default) =>
        Task.FromResult(clients.Values.FirstOrDefault(c => c.Identifier == identifier && c.DeletedAt is null));

    public Task<ClientEntity> SaveAsync(ClientEntity c, CancellationToken ct = default)
    {
        clients[c.Id] = c;
        return Task.FromResult(c);
    }

    public Task SoftDeleteAsync(long id, long deletedBy, int version, CancellationToken ct = default) =>
        Task.CompletedTask;
}

public sealed class FakeGateCacheRepository : IGateCacheRepository
{
    private readonly Dictionary<string, string> store = [];
    public Func<Task>? OnPut;
    public int PutCallCount { get; private set; }

    public Task<string?> GetJwtAsync(string identifier, string memberId, CancellationToken ct = default) =>
        Task.FromResult(store.TryGetValue($"{identifier}:{memberId}", out var v) ? v : null);

    public async Task PutJwtAsync(string identifier, string memberId, string token, long ttlSeconds, CancellationToken ct = default)
    {
        PutCallCount++;
        if (OnPut is not null) await OnPut();
        store[$"{identifier}:{memberId}"] = token;
    }
}

public sealed class FakeJwtHistoryRepository : IJwtHistoryRepository
{
    public readonly List<(long ClientId, string MemberId, string Jwt)> Saved = [];
    public Func<Task>? OnSave;

    public Task<int> CountByConditionAsync(JwtHistoryCondition cond, CancellationToken ct = default) =>
        Task.FromResult(Saved.Count);

    public Task<List<JwtHistory>> FindByConditionAsync(JwtHistoryCondition cond, CancellationToken ct = default) =>
        Task.FromResult(new List<JwtHistory>());

    public async Task SaveAsync(long clientId, string memberId, DateTime issueAt, string jwt, CancellationToken ct = default)
    {
        if (OnSave is not null) await OnSave();
        Saved.Add((clientId, memberId, jwt));
    }
}
