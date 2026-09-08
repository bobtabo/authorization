/*
 * アプリケーション例外モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Support;

/// <summary>HTTP ステータスコードを持つアプリケーション例外です。</summary>
/// <param name="statusCode">HTTPステータスコード</param>
/// <param name="message">エラーメッセージ</param>
public sealed class AppException(int statusCode, string message) : Exception(message)
{
    /// <summary>HTTP ステータスコード</summary>
    public int StatusCode { get; } = statusCode;

    /// <summary>409 Conflict（楽観排他ロック競合）</summary>
    /// <param name="message">エラーメッセージ（既定: optimistic_lock）</param>
    /// <returns>409のAppException</returns>
    public static AppException Conflict(string message = "optimistic_lock") => new(409, message);

    /// <summary>404 Not Found</summary>
    /// <param name="message">エラーメッセージ（既定: not_found）</param>
    /// <returns>404のAppException</returns>
    public static AppException NotFound(string message = "not_found") => new(404, message);

    /// <summary>401 Unauthorized</summary>
    /// <param name="message">エラーメッセージ（既定: unauthenticated）</param>
    /// <returns>401のAppException</returns>
    public static AppException Unauthorized(string message = "unauthenticated") => new(401, message);

    /// <summary>403 Forbidden</summary>
    /// <param name="message">エラーメッセージ（既定: forbidden）</param>
    /// <returns>403のAppException</returns>
    public static AppException Forbidden(string message = "forbidden") => new(403, message);

    /// <summary>400 Bad Request</summary>
    /// <param name="message">エラーメッセージ（既定: bad_request）</param>
    /// <returns>400のAppException</returns>
    public static AppException BadRequest(string message = "bad_request") => new(400, message);
}
