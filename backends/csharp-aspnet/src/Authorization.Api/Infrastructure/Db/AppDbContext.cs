/*
 * EF Core DbContext モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Infrastructure.Model;
using Microsoft.EntityFrameworkCore;

namespace Authorization.Api.Infrastructure.Db;

/// <summary>アプリケーションの DbContext です。</summary>
public sealed class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<ClientModel>       Clients       => Set<ClientModel>();
    public DbSet<StaffModel>        Staffs        => Set<StaffModel>();
    public DbSet<InvitationModel>   Invitations   => Set<InvitationModel>();
    public DbSet<JwtHistoryModel>   JwtHistories  => Set<JwtHistoryModel>();
    public DbSet<NotificationModel> Notifications => Set<NotificationModel>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<ClientModel>().HasIndex(c => c.Identifier).IsUnique();
        modelBuilder.Entity<ClientModel>().HasIndex(c => c.AccessToken).IsUnique();
        modelBuilder.Entity<StaffModel>().HasIndex(s => s.Email).IsUnique();
        modelBuilder.Entity<InvitationModel>().HasIndex(i => i.Token).IsUnique();
    }
}
