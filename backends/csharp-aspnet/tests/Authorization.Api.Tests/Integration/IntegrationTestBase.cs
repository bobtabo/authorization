// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc.Testing;

namespace Authorization.Api.Tests.Integration;

/// <summary>
/// 実MySQL/Redisに接続した本番同等のDI構成でアプリケーションを起動する <see cref="WebApplicationFactory{TEntryPoint}"/> です。
/// </summary>
public sealed class IntegrationWebAppFactory : WebApplicationFactory<Program>
{
    /// <summary>コンストラクタ。<c>Program</c> 起動前に APP_ENV=testing を保証し設定を読み込みます。</summary>
    public IntegrationWebAppFactory()
    {
        _ = TestHelper.Config;
    }
}

/// <summary>
/// 統合テストを直列実行するためのコレクション定義です。
/// 全テストが同一のDB/Redisを共有するため、xUnit の並列実行を無効化します。
/// </summary>
[CollectionDefinition(Name, DisableParallelization = true)]
public sealed class IntegrationCollection : ICollectionFixture<IntegrationWebAppFactory>
{
    /// <summary>コレクション名</summary>
    public const string Name = "Integration";
}

/// <summary>
/// 統合テストの基底クラスです。各テスト前にテーブルをTRUNCATEしRedisをFLUSHDBします。
/// </summary>
/// <param name="factory">アプリケーションファクトリ</param>
[Collection(IntegrationCollection.Name)]
public abstract class IntegrationTestBase(IntegrationWebAppFactory factory)
{
    /// <summary>テスト用HTTPクライアント</summary>
    protected HttpClient Client { get; } = CreateClient(factory);

    private static HttpClient CreateClient(IntegrationWebAppFactory factory)
    {
        TestHelper.TruncateTables();
        return factory.CreateClient();
    }

    /// <summary>リダイレクトを自動追跡しないHTTPクライアントを作成します。</summary>
    /// <returns>HTTPクライアント</returns>
    protected HttpClient CreateNoRedirectClient() =>
        factory.CreateClient(new WebApplicationFactoryClientOptions { AllowAutoRedirect = false });

    /// <summary>staff_id クッキーを付与したリクエストを送信します。</summary>
    /// <param name="method">HTTPメソッド</param>
    /// <param name="url">URL</param>
    /// <param name="staffId">認証済みスタッフID（nullなら未認証）</param>
    /// <param name="body">JSONボディ（nullなら無し）</param>
    /// <returns>HTTPレスポンス</returns>
    protected Task<HttpResponseMessage> SendAsync(HttpMethod method, string url, long? staffId = null, object? body = null)
    {
        var req = new HttpRequestMessage(method, url);
        if (staffId is not null) req.Headers.Add("Cookie", $"staff_id={staffId}");
        if (body is not null) req.Content = JsonContent.Create(body);
        return Client.SendAsync(req);
    }

    /// <summary>レスポンスボディをJSONとして読み取ります。</summary>
    /// <param name="res">HTTPレスポンス</param>
    /// <returns>JSONルート要素</returns>
    protected static async Task<JsonElement> ReadJsonAsync(HttpResponseMessage res)
    {
        using var doc = JsonDocument.Parse(await res.Content.ReadAsStringAsync());
        return doc.RootElement.Clone();
    }
}
