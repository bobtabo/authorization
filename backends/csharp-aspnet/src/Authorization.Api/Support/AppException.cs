/*
 * アプリケーション例外モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Support;

/// <summary>HTTP ステータスコードを持つアプリケーション例外です。</summary>
public sealed class AppException(int statusCode, string message) : Exception(message)
{
    /// <summary>HTTP ステータスコード</summary>
    public int StatusCode { get; } = statusCode;

    /// <summary>409 Conflict（楽観排他ロック競合）</summary>
    public static AppException Conflict(string message = "optimistic_lock") => new(409, message);

    /// <summary>404 Not Found</summary>
    public static AppException NotFound(string message = "not_found") => new(404, message);

    /// <summary>401 Unauthorized</summary>
    public static AppException Unauthorized(string message = "unauthenticated") => new(401, message);

    /// <summary>403 Forbidden</summary>
    public static AppException Forbidden(string message = "forbidden") => new(403, message);

    /// <summary>400 Bad Request</summary>
    public static AppException BadRequest(string message = "bad_request") => new(400, message);
}
