using Authorization.Api.Domain.Staff;
using Authorization.Api.Support;
using Authorization.Api.Tests.UseCase.Staff;
using Authorization.Api.UseCase.Auth;

namespace Authorization.Api.Tests.UseCase.Auth;

public class AuthInteractorTests
{
    private static Authorization.Api.Domain.Staff.Staff MakeStaff(long id, int provider, string providerId) => new()
    {
        Id         = id,
        Name       = $"Staff {id}",
        Email      = $"staff{id}@example.com",
        Provider   = provider,
        ProviderId = providerId,
        Role       = StaffRole.Member,
    };

    [Fact]
    public async Task FindUserAsync_NotFound_ThrowsNotFound()
    {
        var uc = new AuthInteractor(new FakeStaffRepository(), new FakeInvitationAuthRepository());

        var ex = await Assert.ThrowsAsync<AppException>(() => uc.FindUserAsync(999));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task LoginAsync_ExistingStaff_UpdatesAvatarAndLastLogin()
    {
        var staffRepo = new FakeStaffRepository().Add(MakeStaff(1, StaffProvider.Google, "g-1"));
        var uc = new AuthInteractor(staffRepo, new FakeInvitationAuthRepository());

        var staff = await uc.LoginAsync(new LoginDto(StaffProvider.Google, "g-1", "New Name", "new@example.com", "avatar.png"));

        Assert.Equal(1, staff.Id);
        Assert.Equal("avatar.png", staff.Avatar);
    }

    [Fact]
    public async Task LoginAsync_NewStaffWithoutInvitationToken_ThrowsForbidden()
    {
        var uc = new AuthInteractor(new FakeStaffRepository(), new FakeInvitationAuthRepository());

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.LoginAsync(new LoginDto(StaffProvider.Google, "g-new", "Name", "a@example.com", null)));

        Assert.Equal(403, ex.StatusCode);
    }

    [Fact]
    public async Task LoginAsync_NewStaffWithInvalidInvitationToken_ThrowsForbidden()
    {
        var uc = new AuthInteractor(new FakeStaffRepository(), new FakeInvitationAuthRepository());

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.LoginAsync(new LoginDto(StaffProvider.Google, "g-new", "Name", "a@example.com", null, InvitationToken: "unknown-token")));

        Assert.Equal(403, ex.StatusCode);
    }

    [Fact]
    public async Task LoginAsync_NewStaffWithValidInvitationToken_CreatesStaffAndRemovesToken()
    {
        var staffRepo      = new FakeStaffRepository();
        var invitationRepo = new FakeInvitationAuthRepository().Add("tok-1", StaffRole.Admin);
        var uc = new AuthInteractor(staffRepo, invitationRepo);

        var staff = await uc.LoginAsync(new LoginDto(StaffProvider.Google, "g-new", "New", "new@example.com", null, InvitationToken: "tok-1"));

        Assert.Equal(StaffRole.Admin, staff.Role);
        Assert.Contains("tok-1", invitationRepo.Removed);
    }

    [Fact]
    public async Task LoginAsync_NewStaffWithNonAdminRoleValue_RoundsToMember()
    {
        var staffRepo      = new FakeStaffRepository();
        var invitationRepo = new FakeInvitationAuthRepository().Add("tok-1", 999);
        var uc = new AuthInteractor(staffRepo, invitationRepo);

        var staff = await uc.LoginAsync(new LoginDto(StaffProvider.Google, "g-new", "New", "new@example.com", null, InvitationToken: "tok-1"));

        Assert.Equal(StaffRole.Member, staff.Role);
    }
}
