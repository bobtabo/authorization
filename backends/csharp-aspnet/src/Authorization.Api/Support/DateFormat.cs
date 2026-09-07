/*
 * 日時フォーマットモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Globalization;

namespace Authorization.Api.Support;

/// <summary>API レスポンスで使う日時フォーマットです。</summary>
public static class DateFormat
{
    private const string Minute = "yyyy-MM-dd HH:mm";
    private const string Second = "yyyy-MM-dd HH:mm:ss";

    /// <summary>yyyy-MM-dd HH:mm 形式</summary>
    public static string ToMinute(DateTime dt) => dt.ToString(Minute, CultureInfo.InvariantCulture);

    /// <summary>yyyy-MM-dd HH:mm 形式（null は null のまま）</summary>
    public static string? ToMinute(DateTime? dt) => dt.HasValue ? ToMinute(dt.Value) : null;

    /// <summary>yyyy-MM-dd HH:mm:ss 形式</summary>
    public static string ToSecond(DateTime dt) => dt.ToString(Second, CultureInfo.InvariantCulture);

    /// <summary>
    /// yyyy-MM-dd 形式の日付文字列を DateTime に変換します。
    /// 変換できない場合は null を返します。
    /// </summary>
    public static DateTime? ParseDate(string? value)
    {
        if (string.IsNullOrWhiteSpace(value)) return null;
        return DateTime.TryParse(value, CultureInfo.InvariantCulture, DateTimeStyles.None, out var dt) ? dt : null;
    }
}
