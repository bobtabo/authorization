// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using Authorization.Api.Config;
using Authorization.Api.Domain.Client;
using Authorization.Api.Domain.Notification;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Infrastructure.Model;
using Authorization.Api.UseCase.Client;
using Microsoft.EntityFrameworkCore;
using StackExchange.Redis;
using MySqlServerType = Pomelo.EntityFrameworkCore.MySql.Infrastructure.ServerType;

namespace Authorization.Api.Tests.Integration;

/// <summary>
/// 統合テスト用ヘルパーです（Kotlin版 TestHelper 相当）。
/// 実MySQL/Redisに接続し、テーブル初期化・レコード作成・キャッシュ削除を行います。
/// </summary>
public static class TestHelper
{
    private static readonly Lazy<AppConfig> LazyConfig = new(() =>
    {
        if (string.IsNullOrEmpty(Environment.GetEnvironmentVariable("APP_ENV")))
        {
            Environment.SetEnvironmentVariable("APP_ENV", "testing");
        }
        return ConfigLoader.Load();
    });

    private static readonly Lazy<IConnectionMultiplexer> LazyRedis = new(() =>
    {
        var options = new ConfigurationOptions
        {
            EndPoints          = { { Config.Redis.Host, Config.Redis.Port } },
            DefaultDatabase    = Config.Redis.Database,
            AllowAdmin         = true,
            AbortOnConnectFail = false,
        };
        if (!string.IsNullOrWhiteSpace(Config.Redis.Password)) options.Password = Config.Redis.Password;
        return ConnectionMultiplexer.Connect(options);
    });

    private static readonly Lazy<(string PrivatePem, string PublicPem, string Fingerprint)> LazyKeys =
        new(ClientService.GenerateRsaKeys);

    private static readonly Lazy<bool> LazySchema = new(() =>
    {
        using var db = NewDbContext();
        db.Database.EnsureCreated();
        return true;
    });

    /// <summary>テスト用アプリケーション設定（.env.testing + 環境変数）</summary>
    public static AppConfig Config => LazyConfig.Value;

    /// <summary>Redis接続</summary>
    public static IConnectionMultiplexer Redis => LazyRedis.Value;

    /// <summary>テスト間で再利用するRSA鍵ペア（生成コスト削減のためキャッシュ）</summary>
    public static (string PrivatePem, string PublicPem, string Fingerprint) Keys => LazyKeys.Value;

    /// <summary>実MySQLに接続する <see cref="AppDbContext"/> を生成します。</summary>
    /// <returns>DbContext</returns>
    public static AppDbContext NewDbContext()
    {
        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseMySql(Config.Db.ConnectionString, ServerVersion.Create(new Version(8, 0), MySqlServerType.MySql))
            .Options;
        return new AppDbContext(options);
    }

    /// <summary>スキーマを作成し（未作成の場合のみ）、全テーブルをTRUNCATEしてRedisをFLUSHDBします。</summary>
    public static void TruncateTables()
    {
        _ = LazySchema.Value;

        using var db = NewDbContext();
        var tables = db.Model.GetEntityTypes()
            .Select(e => e.GetTableName())
            .Where(t => t is not null)
            .Cast<string>()
            .ToList();

        var sql = "SET FOREIGN_KEY_CHECKS=0;\n"
                + string.Join("\n", tables.Select(t => $"TRUNCATE TABLE `{t}`;"))
                + "\nSET FOREIGN_KEY_CHECKS=1;";
        db.Database.ExecuteSqlRaw(sql);

        var endpoint = Redis.GetEndPoints()[0];
        Redis.GetServer(endpoint).FlushDatabase(Config.Redis.Database);
    }

    /// <summary>スタッフレコードを作成します。</summary>
    /// <param name="name">氏名</param>
    /// <param name="email">メールアドレス</param>
    /// <param name="role">ロール</param>
    /// <param name="providerId">OAuthプロバイダーID</param>
    /// <param name="deleted">論理削除済みにするか</param>
    /// <returns>作成したスタッフID</returns>
    public static long CreateStaff(string name = "Test Staff", string email = "staff@example.com",
        int role = StaffRole.Admin, string providerId = "google-1", bool deleted = false)
    {
        var now = DateTime.Now;
        var staff = new StaffModel
        {
            Name       = name,
            Email      = email,
            Provider   = StaffProvider.Google,
            ProviderId = providerId,
            Role       = role,
            CreatedAt  = now,
            UpdatedAt  = now,
            DeletedAt  = deleted ? now : null,
            Version    = 1,
        };
        using var db = NewDbContext();
        db.Staffs.Add(staff);
        db.SaveChanges();
        return staff.Id;
    }

    /// <summary>クライアントレコードを作成します（RSA鍵はキャッシュ済みのものを使用）。</summary>
    /// <param name="name">名称</param>
    /// <param name="identifier">識別子</param>
    /// <param name="accessToken">アクセストークン</param>
    /// <param name="status">ステータス</param>
    /// <returns>作成したクライアントID</returns>
    public static long CreateClient(string name = "Test Client", string identifier = "client-1",
        string accessToken = "access-token-1", int status = ClientStatus.Active)
    {
        var now = DateTime.Now;
        var client = new ClientModel
        {
            Name        = name,
            Identifier  = identifier,
            PostCode    = "1000001",
            Pref        = "東京都",
            City        = "千代田区",
            Address     = "1-1-1",
            Building    = null,
            Tel         = "0312345678",
            Email       = "client@example.com",
            AccessToken = accessToken,
            PrivateKey  = Keys.PrivatePem,
            PublicKey   = Keys.PublicPem,
            Fingerprint = Keys.Fingerprint,
            Status      = status,
            StartAt     = status == ClientStatus.Active ? now : null,
            CreatedAt   = now,
            UpdatedAt   = now,
            Version     = 1,
        };
        using var db = NewDbContext();
        db.Clients.Add(client);
        db.SaveChanges();
        return client.Id;
    }

    /// <summary>招待レコードを作成します。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="role">招待ロール</param>
    /// <returns>作成した招待ID</returns>
    public static int CreateInvitation(string token = "test-invitation-token", int role = StaffRole.Member)
    {
        var now = DateTime.Now;
        var invitation = new InvitationModel
        {
            Token     = token,
            Role      = role,
            CreatedAt = now,
            UpdatedAt = now,
            Version   = 1,
        };
        using var db = NewDbContext();
        db.Invitations.Add(invitation);
        db.SaveChanges();
        return invitation.Id;
    }

    /// <summary>通知レコードを作成します。</summary>
    /// <param name="staffId">宛先スタッフID</param>
    /// <param name="title">タイトル</param>
    /// <param name="message">本文</param>
    /// <param name="read">既読か</param>
    /// <returns>作成した通知ID</returns>
    public static long CreateNotification(long staffId, string title = "Test Notification",
        string message = "Test message", bool read = false)
    {
        var now = DateTime.Now;
        var notification = new NotificationModel
        {
            StaffId     = staffId,
            MessageType = NotificationMessageType.Info,
            Title       = title,
            Message     = message,
            Url         = null,
            Read        = read,
            CreatedAt   = now,
            CreatedBy   = 0,
            UpdatedAt   = now,
            UpdatedBy   = 0,
            Version     = 1,
        };
        using var db = NewDbContext();
        db.Notifications.Add(notification);
        db.SaveChanges();
        return notification.Id;
    }
}
