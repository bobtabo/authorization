using Authorization.Api.Domain.Invitation;

namespace Authorization.Api.Tests.UseCase.Invitation;

public sealed class FakeInvitationRepository : IInvitationRepository
{
    private readonly Dictionary<int, InvitationVo> currentByRole = [];
    private readonly Dictionary<string, InvitationVo> byToken = [];
    public int IssueCallCount { get; private set; }

    public FakeInvitationRepository Add(InvitationVo vo, bool isCurrent = true)
    {
        byToken[vo.Token] = vo;
        if (isCurrent) currentByRole[vo.Role] = vo;
        return this;
    }

    public Task<InvitationVo?> GetCurrentByRoleAsync(int role, CancellationToken ct = default) =>
        Task.FromResult(currentByRole.TryGetValue(role, out var v) ? v : null);

    public Task<InvitationVo> IssueAsync(int role, CancellationToken ct = default)
    {
        IssueCallCount++;
        var vo = new InvitationVo($"token-{role}-{IssueCallCount}", role, "https://example.com/i/x", "example.com/i/x");
        currentByRole[role] = vo;
        byToken[vo.Token] = vo;
        return Task.FromResult(vo);
    }

    public Task<InvitationVo?> FindByTokenAsync(string token, CancellationToken ct = default) =>
        Task.FromResult(byToken.TryGetValue(token, out var v) ? v : null);
}
