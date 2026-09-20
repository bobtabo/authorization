using Authorization.Api.Domain.Invitation;

namespace Authorization.Api.Tests.UseCase.Auth;

public sealed class FakeInvitationAuthRepository : IInvitationAuthRepository
{
    private readonly Dictionary<string, int> roles = [];
    public readonly List<string> Removed = [];
    public readonly Dictionary<string, long> Ttls = [];

    public FakeInvitationAuthRepository Add(string token, int role)
    {
        roles[token] = role;
        return this;
    }

    public Task PutRoleAsync(string token, int role, long ttlSeconds, CancellationToken ct = default)
    {
        roles[token] = role;
        Ttls[token] = ttlSeconds;
        return Task.CompletedTask;
    }

    public Task<int?> GetRoleAsync(string token, CancellationToken ct = default) =>
        Task.FromResult(roles.TryGetValue(token, out var r) ? r : (int?)null);

    public Task RemoveAsync(string token, CancellationToken ct = default)
    {
        Removed.Add(token);
        roles.Remove(token);
        return Task.CompletedTask;
    }
}
