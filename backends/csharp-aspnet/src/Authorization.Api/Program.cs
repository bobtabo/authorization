/*
 * アプリケーションエントリポイント。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Globalization;
using Authorization.Api;
using Authorization.Api.Config;

var cfg = ConfigLoader.Load();

CultureInfo.DefaultThreadCurrentCulture   = CultureInfo.InvariantCulture;
CultureInfo.DefaultThreadCurrentUICulture = CultureInfo.InvariantCulture;
if (!TimeZoneInfo.TryFindSystemTimeZoneById(cfg.App.Timezone, out var tz))
    throw new InvalidOperationException($"unknown timezone: {cfg.App.Timezone}");
Environment.SetEnvironmentVariable("TZ", cfg.App.Timezone);

var builder = WebApplication.CreateBuilder(args);
builder.WebHost.UseUrls($"http://0.0.0.0:{cfg.App.Port}");
builder.Services.AddAppServices(cfg);

var app = builder.Build();
app.UseAppExceptionHandler();
app.MapAppRoutes();
app.Logger.LogInformation("timezone={Tz}", tz?.Id ?? cfg.App.Timezone);
app.Run();
