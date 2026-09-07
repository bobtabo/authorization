/*
 * DI 登録・ルーティング・例外ハンドリングモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Text.Json;
using Authorization.Api.Config;
using Authorization.Api.Domain.Client;
using Authorization.Api.Domain.Gate;
using Authorization.Api.Domain.Invitation;
using Authorization.Api.Domain.Notification;
using Authorization.Api.Domain.Staff;
using Authorization.Api.Handler;
using Authorization.Api.Infrastructure.Cache;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Infrastructure.Mail;
using Authorization.Api.Infrastructure.Persistence;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Auth;
using Authorization.Api.UseCase.Client;
using Authorization.Api.UseCase.Gate;
using Authorization.Api.UseCase.Invitation;
using Authorization.Api.UseCase.Notification;
using Authorization.Api.UseCase.Staff;
using Microsoft.EntityFrameworkCore;
using StackExchange.Redis;

namespace Authorization.Api;

/// <summary>アプリケーションの構成（DI・ルーティング・例外処理）です。</summary>
public static class AppModule
{
    /// <summary>設定・インフラ・ユースケース・ハンドラーを DI コンテナへ登録します。</summary>
    public static IServiceCollection AddAppServices(this IServiceCollection services, AppConfig cfg)
    {
        services.AddSingleton(cfg);
        services.AddSingleton(cfg.App);
        services.AddSingleton(cfg.Db);
        services.AddSingleton(cfg.Redis);
        services.AddSingleton(cfg.OAuth);
        services.AddSingleton(cfg.Jwt);
        services.AddSingleton(cfg.Mail);
        services.AddSingleton(cfg.Aws);

        services.AddDbContext<AppDbContext>(o =>
            o.UseMySql(cfg.Db.ConnectionString, ServerVersion.Create(new Version(8, 0), Pomelo.EntityFrameworkCore.MySql.Infrastructure.ServerType.MySql)));
        services.AddSingleton<IConnectionMultiplexer>(_ => RedisConnection.Connect(cfg.Redis));
        services.AddHttpClient<IOAuthClient, HttpOAuthClient>();

        services.AddScoped<IClientRepository, EfClientRepository>();
        services.AddScoped<IStaffRepository, EfStaffRepository>();
        services.AddScoped<IInvitationRepository, EfInvitationRepository>();
        services.AddScoped<INotificationRepository, EfNotificationRepository>();
        services.AddScoped<IJwtHistoryRepository, EfJwtHistoryRepository>();
        services.AddSingleton<IGateCacheRepository, RedisGateRepository>();
        services.AddSingleton<IInvitationAuthRepository, RedisInvitationAuthRepository>();
        services.AddSingleton<IMailer, SesMailer>();

        services.AddScoped<AuthInteractor>();
        services.AddScoped<ClientInteractor>();
        services.AddScoped<StaffInteractor>();
        services.AddScoped<InvitationInteractor>();
        services.AddScoped<NotificationInteractor>();
        services.AddScoped(sp => new GateInteractor(
            sp.GetRequiredService<IClientRepository>(),
            sp.GetRequiredService<IGateCacheRepository>(),
            cfg.Jwt,
            sp.GetRequiredService<IJwtHistoryRepository>(),
            sp.GetRequiredService<ILogger<GateInteractor>>()));

        services.AddScoped<AuthHandler>();
        services.AddScoped<ClientHandler>();
        services.AddScoped<StaffHandler>();
        services.AddScoped<AdminInvitationHandler>();
        services.AddScoped<GateHandler>();
        services.AddScoped<NotificationHandler>();

        services.ConfigureHttpJsonOptions(o => o.SerializerOptions.PropertyNamingPolicy = null);
        return services;
    }

    /// <summary>AppException を {"error": message} 形式の JSON に変換する例外ハンドラーを登録します。</summary>
    public static IApplicationBuilder UseAppExceptionHandler(this IApplicationBuilder app) =>
        app.Use(async (ctx, next) =>
        {
            try
            {
                await next(ctx);
            }
            catch (AppException e)
            {
                ctx.Response.StatusCode  = e.StatusCode;
                ctx.Response.ContentType = "application/json; charset=utf-8";
                await ctx.Response.WriteAsync(JsonSerializer.Serialize(new Dictionary<string, string> { ["error"] = e.Message }));
            }
            catch (Exception e) when (e is not OperationCanceledException)
            {
                ctx.RequestServices.GetRequiredService<ILogger<AppException>>().LogError(e, "unhandled error");
                ctx.Response.StatusCode  = 500;
                ctx.Response.ContentType = "application/json; charset=utf-8";
                await ctx.Response.WriteAsync(JsonSerializer.Serialize(new Dictionary<string, string> { ["error"] = "internal_server_error" }));
            }
        });

    /// <summary>API ルートを登録します。</summary>
    public static IEndpointRouteBuilder MapAppRoutes(this IEndpointRouteBuilder app)
    {
        // OAuth（ブラウザリダイレクトのため /api 外）
        app.MapGet("/auth/google/redirect", (HttpRequest req, AuthHandler h) => h.GoogleRedirect(req));
        app.MapGet("/auth/google/callback", (HttpRequest req, AuthHandler h, CancellationToken ct) => h.GoogleCallbackAsync(req, ct));
        app.MapGet("/auth/github/redirect", (HttpRequest req, AuthHandler h) => h.GithubRedirect(req));
        app.MapGet("/auth/github/callback", (HttpRequest req, AuthHandler h, CancellationToken ct) => h.GithubCallbackAsync(req, ct));

        var api = app.MapGroup("/api");

        // --- auth ---
        api.MapGet("/auth/me",                 (HttpRequest req, AuthHandler h, CancellationToken ct) => h.ProfileAsync(req, ct));
        api.MapGet("/auth/login",              (HttpRequest req, AuthHandler h, CancellationToken ct) => h.ProfileAsync(req, ct));
        api.MapGet("/auth/logout",             (HttpResponse res, AuthHandler h) => h.Logout(res));
        api.MapGet("/auth/invitation/{token}", (string token, AuthHandler h, CancellationToken ct) => h.InvitationAsync(token, ct));

        // --- clients ---
        api.MapGet("/clients",                     (HttpRequest req, ClientHandler h, CancellationToken ct) => h.IndexAsync(req, ct));
        api.MapPost("/clients/store",              (HttpRequest req, ClientHandler h, CancellationToken ct) => h.StoreAsync(req, ct));
        api.MapPut("/clients/{id}/update",         (string id, HttpRequest req, ClientHandler h, CancellationToken ct) => h.UpdateAsync(id, req, ct));
        api.MapGet("/clients/{id}",                (string id, ClientHandler h, CancellationToken ct) => h.ShowAsync(id, ct));
        api.MapDelete("/clients/{id}/delete",      (string id, HttpRequest req, ClientHandler h, CancellationToken ct) => h.DestroyAsync(id, req, ct));
        api.MapGet("/clients/{id}/jwt-histories",  (string id, HttpRequest req, ClientHandler h, CancellationToken ct) => h.JwtHistoriesAsync(id, req, ct));

        // --- clients（スマホ連携）---
        api.MapGet("/clients/{identifier}/qr",      (string identifier, ClientHandler h, CancellationToken ct) => h.QrAsync(identifier, ct));
        api.MapGet("/clients/{identifier}/info",    (string identifier, ClientHandler h, CancellationToken ct) => h.InfoAsync(identifier, ct));
        api.MapPatch("/clients/{identifier}/start", (string identifier, ClientHandler h, CancellationToken ct) => h.StartAsync(identifier, ct));
        api.MapPatch("/clients/{identifier}/stop",  (string identifier, ClientHandler h, CancellationToken ct) => h.StopAsync(identifier, ct));

        // --- staffs ---
        api.MapGet("/staffs",                   (HttpRequest req, StaffHandler h, CancellationToken ct) => h.IndexAsync(req, ct));
        api.MapPatch("/staffs/{id}/updateRole", (string id, HttpRequest req, StaffHandler h, CancellationToken ct) => h.UpdateRoleAsync(id, req, ct));
        api.MapPatch("/staffs/{id}/restore",    (string id, StaffHandler h, CancellationToken ct) => h.RestoreAsync(id, ct));
        api.MapDelete("/staffs/{id}/delete",    (string id, HttpRequest req, StaffHandler h, CancellationToken ct) => h.DestroyAsync(id, req, ct));

        // --- admin ---
        api.MapGet("/admin/invitation",       (HttpRequest req, AdminInvitationHandler h, CancellationToken ct) => h.IndexAsync(req, ct));
        api.MapGet("/admin/invitation/issue", (HttpRequest req, AdminInvitationHandler h, CancellationToken ct) => h.IssueAsync(req, ct));

        // --- gate ---
        api.MapGet("/gate/issue",                      (HttpRequest req, GateHandler h, CancellationToken ct) => h.IssueAsync(req, ct));
        api.MapGet("/gate/client/{identifier}/verify", (string identifier, HttpRequest req, GateHandler h, CancellationToken ct) => h.VerifyAsync(identifier, req, ct));

        // --- notifications ---
        api.MapGet("/notifications/counts", (HttpRequest req, NotificationHandler h, CancellationToken ct) => h.CountsAsync(req, ct));
        api.MapGet("/notifications",        (HttpRequest req, NotificationHandler h, CancellationToken ct) => h.IndexAsync(req, ct));
        api.MapPatch("/notifications",      (HttpRequest req, NotificationHandler h, CancellationToken ct) => h.ReadAllAsync(req, ct));
        api.MapPatch("/notifications/{id}", (string id, NotificationHandler h, CancellationToken ct) => h.ReadAsync(id, ct));

        return app;
    }
}
