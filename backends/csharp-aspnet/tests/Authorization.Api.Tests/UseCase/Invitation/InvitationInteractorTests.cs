using Authorization.Api.Domain.Invitation;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Authorization.Api.Tests.UseCase.Auth;
using Authorization.Api.UseCase.Invitation;

namespace Authorization.Api.Tests.UseCase.Invitation;

public class InvitationInteractorTests
{
    [Fact]
    public async Task CurrentAsync_NotFound_ThrowsNotFound()
    {
        var uc = new InvitationInteractor(new FakeInvitationRepository(), new FakeInvitationAuthRepository());

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.CurrentAsync(StaffRole.Admin));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task CurrentAsync_Found_ReturnsInvitation()
    {
        var vo = new InvitationVo("tok-1", StaffRole.Admin, "https://example.com/i/tok-1", "example.com/i/tok-1");
        var repo = new FakeInvitationRepository().Add(vo);
        var uc = new InvitationInteractor(repo, new FakeInvitationAuthRepository());

        var result = await uc.CurrentAsync(StaffRole.Admin);

        Assert.Equal("tok-1", result.Token);
    }

    [Fact]
    public async Task IssueAsync_DelegatesToRepository()
    {
        var repo = new FakeInvitationRepository();
        var uc = new InvitationInteractor(repo, new FakeInvitationAuthRepository());

        var result = await uc.IssueAsync(StaffRole.Member);

        Assert.Equal(StaffRole.Member, result.Role);
        Assert.Equal(1, repo.IssueCallCount);
    }

    [Fact]
    public async Task FindByTokenAsync_NotFound_ThrowsNotFound()
    {
        var uc = new InvitationInteractor(new FakeInvitationRepository(), new FakeInvitationAuthRepository());

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.FindByTokenAsync("unknown"));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task FindByTokenAsync_Found_CachesRoleForLogin()
    {
        var vo = new InvitationVo("tok-1", StaffRole.Admin, "https://example.com/i/tok-1", "example.com/i/tok-1");
        var repo = new FakeInvitationRepository().Add(vo);
        var authRepo = new FakeInvitationAuthRepository();
        var uc = new InvitationInteractor(repo, authRepo);

        var result = await uc.FindByTokenAsync("tok-1");

        Assert.Equal("tok-1", result.Token);
        Assert.Equal(StaffRole.Admin, await authRepo.GetRoleAsync("tok-1"));
    }
}
