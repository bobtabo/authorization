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
    /// <param name="req">HTTPリクエスト</param>
    /// <returns>スタッフID、無い・不正な場合は0</returns>
    public static long StaffId(HttpRequest req) =>
        req.Cookies.TryGetValue("staff_id", out var v) && long.TryParse(v, out var id) ? id : 0;

    /// <summary>クエリ文字列を取得します（無ければ null）。</summary>
    /// <param name="req">HTTPリクエスト</param>
    /// <param name="key">クエリパラメータ名</param>
    /// <returns>クエリ値、無ければnull</returns>
    public static string? Query(HttpRequest req, string key) =>
        req.Query.TryGetValue(key, out var v) ? v.ToString() : null;

    /// <summary>クエリ文字列を int として取得します。</summary>
    /// <param name="req">HTTPリクエスト</param>
    /// <param name="key">クエリパラメータ名</param>
    /// <returns>クエリ値のint表現、無い・不正な場合はnull</returns>
    public static int? QueryInt(HttpRequest req, string key) =>
        int.TryParse(Query(req, key), out var v) ? v : null;

    /// <summary>limit と page からoffsetを計算します。int（32bit）の範囲を超える場合はnullを返します。</summary>
    /// <param name="limit">1ページの件数</param>
    /// <param name="page">ページ番号（1始まり）</param>
    /// <returns>offset、int の範囲を超える場合はnull</returns>
    public static int? SafeOffset(int limit, int page)
    {
        var offset = (long)limit * (page - 1);
        return offset is >= int.MinValue and <= int.MaxValue ? (int)offset : null;
    }

    /// <summary>{"error": message} を返します。</summary>
    /// <param name="status">HTTPステータスコード</param>
    /// <param name="message">エラーメッセージ</param>
    /// <returns>エラーJSONレスポンス</returns>
    public static IResult Error(int status, string message) =>
        Results.Json(new Dictionary<string, object?> { ["error"] = message }, statusCode: status);

    /// <summary>401 {"error":"unauthenticated"}</summary>
    /// <returns>未認証エラーレスポンス</returns>
    public static IResult Unauthenticated() => Error(401, "unauthenticated");

    /// <summary>400 {"error":"invalid_id"}</summary>
    /// <returns>ID不正エラーレスポンス</returns>
    public static IResult InvalidId() => Error(400, "invalid_id");

    /// <summary>空 JSON オブジェクトを返します。</summary>
    /// <param name="status">HTTPステータスコード（既定200）</param>
    /// <returns>空JSONレスポンス</returns>
    public static IResult Empty(int status = 200) => Results.Json(new Dictionary<string, object?>(), statusCode: status);

    /// <summary>リクエストボディを JSON オブジェクトとして読み取ります。空・不正な場合は空オブジェクト。</summary>
    /// <param name="req">HTTPリクエスト</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>パース済みJSONオブジェクト、空・不正な場合は空オブジェクト</returns>
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
    /// <param name="obj">JSONオブジェクト</param>
    /// <param name="key">プロパティ名</param>
    /// <returns>文字列値、null/欠損の場合はnull</returns>
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
    /// <param name="obj">JSONオブジェクト</param>
    /// <param name="key">プロパティ名</param>
    /// <returns>整数値、null/欠損/不正な場合はnull</returns>
    public static int? Int(this JsonObject obj, string key)
    {
        if (!obj.TryGetPropertyValue(key, out var node) || node is not JsonValue v) return null;
        if (v.TryGetValue<int>(out var i)) return i;
        if (v.TryGetValue<string>(out var s) && int.TryParse(s, out var parsed)) return parsed;
        return null;
    }
}
