/*
 * ハンドラー共通ヘルパーモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Text.Json;
using System.Text.Json.Nodes;

namespace Authorization.Api.Handler;

/// <summary>ハンドラー共通のリクエスト/レスポンス補助です。</summary>
public static class HttpHelpers
{
    /// <summary>staff_id クッキーを読み取ります。無い・不正な場合は 0。</summary>
    public static long StaffId(HttpRequest req) =>
        req.Cookies.TryGetValue("staff_id", out var v) && long.TryParse(v, out var id) ? id : 0;

    /// <summary>クエリ文字列を取得します（無ければ null）。</summary>
    public static string? Query(HttpRequest req, string key) =>
        req.Query.TryGetValue(key, out var v) ? v.ToString() : null;

    /// <summary>クエリ文字列を int として取得します。</summary>
    public static int? QueryInt(HttpRequest req, string key) =>
        int.TryParse(Query(req, key), out var v) ? v : null;

    /// <summary>{"error": message} を返します。</summary>
    public static IResult Error(int status, string message) =>
        Results.Json(new Dictionary<string, object?> { ["error"] = message }, statusCode: status);

    /// <summary>401 {"error":"unauthenticated"}</summary>
    public static IResult Unauthenticated() => Error(401, "unauthenticated");

    /// <summary>400 {"error":"invalid_id"}</summary>
    public static IResult InvalidId() => Error(400, "invalid_id");

    /// <summary>空 JSON オブジェクトを返します。</summary>
    public static IResult Empty(int status = 200) => Results.Json(new Dictionary<string, object?>(), statusCode: status);

    /// <summary>リクエストボディを JSON オブジェクトとして読み取ります。空・不正な場合は空オブジェクト。</summary>
    public static async Task<JsonObject> ReadJsonObjectAsync(HttpRequest req, CancellationToken ct)
    {
        try
        {
            var node = await JsonNode.ParseAsync(req.Body, cancellationToken: ct);
            return node as JsonObject ?? [];
        }
        catch (JsonException)
        {
            return [];
        }
    }

    /// <summary>JSON の文字列値を取得します（数値等は文字列化、null/欠損は null）。</summary>
    public static string? Str(this JsonObject obj, string key)
    {
        if (!obj.TryGetPropertyValue(key, out var node) || node is null) return null;
        if (node is JsonValue v)
        {
            if (v.TryGetValue<string>(out var s)) return s;
            return v.ToJsonString();
        }
        return null;
    }

    /// <summary>JSON の整数値を取得します（数値文字列も許容）。</summary>
    public static int? Int(this JsonObject obj, string key)
    {
        if (!obj.TryGetPropertyValue(key, out var node) || node is not JsonValue v) return null;
        if (v.TryGetValue<int>(out var i)) return i;
        if (v.TryGetValue<string>(out var s) && int.TryParse(s, out var parsed)) return parsed;
        return null;
    }
}
