/*
 * 日時フォーマットモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Globalization;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Authorization.Api.Support;

/// <summary>API レスポンスで使う日時フォーマットです。</summary>
public static class DateFormat
{
    private const string Minute = "yyyy-MM-dd HH:mm";
    private const string Second = "yyyy-MM-dd HH:mm:ss";

    /// <summary>
    /// VOをそのままJSON化するためのシリアライズ設定です。プロパティ名はsnake_caseに変換し、
    /// DateTime/DateTime? は<see cref="ToMinute(DateTime)"/>形式の文字列にします。
    /// クラスプロパティを1件ずつ辞書に並べ直す手書きコードを避けるために使います。
    /// </summary>
    public static readonly JsonSerializerOptions SnakeCaseMinuteJsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower,
        Converters = { new MinuteDateTimeConverter(), new MinuteNullableDateTimeConverter() },
    };

    /// <summary>yyyy-MM-dd HH:mm 形式</summary>
    /// <param name="dt">日時</param>
    /// <returns>フォーマット済み文字列</returns>
    public static string ToMinute(DateTime dt) => dt.ToString(Minute, CultureInfo.InvariantCulture);

    /// <summary>yyyy-MM-dd HH:mm 形式（null は null のまま）</summary>
    /// <param name="dt">日時（null可）</param>
    /// <returns>フォーマット済み文字列、nullの場合はnull</returns>
    public static string? ToMinute(DateTime? dt) => dt.HasValue ? ToMinute(dt.Value) : null;

    /// <summary>yyyy-MM-dd HH:mm:ss 形式</summary>
    /// <param name="dt">日時</param>
    /// <returns>フォーマット済み文字列</returns>
    public static string ToSecond(DateTime dt) => dt.ToString(Second, CultureInfo.InvariantCulture);

    /// <summary>
    /// yyyy-MM-dd 形式の日付文字列を DateTime に変換します。
    /// 変換できない場合は null を返します。
    /// </summary>
    /// <param name="value">日付文字列</param>
    /// <returns>変換したDateTime、変換できない場合はnull</returns>
    public static DateTime? ParseDate(string? value)
    {
        if (string.IsNullOrWhiteSpace(value)) return null;
        return DateTime.TryParse(value, CultureInfo.InvariantCulture, DateTimeStyles.None, out var dt) ? dt : null;
    }

    /// <summary>DateTime を <see cref="ToMinute(DateTime)"/> 形式の文字列としてシリアライズします。</summary>
    private sealed class MinuteDateTimeConverter : JsonConverter<DateTime>
    {
        /// <inheritdoc/>
        /// <exception cref="NotSupportedException">レスポンス専用のため読み込みは未対応です</exception>
        public override DateTime Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
            => throw new NotSupportedException("SnakeCaseMinuteJsonOptions はレスポンス専用です。");

        /// <inheritdoc/>
        public override void Write(Utf8JsonWriter writer, DateTime value, JsonSerializerOptions options)
            => writer.WriteStringValue(ToMinute(value));
    }

    /// <summary>DateTime? を <see cref="ToMinute(DateTime?)"/> 形式の文字列としてシリアライズします。</summary>
    private sealed class MinuteNullableDateTimeConverter : JsonConverter<DateTime?>
    {
        /// <inheritdoc/>
        /// <exception cref="NotSupportedException">レスポンス専用のため読み込みは未対応です</exception>
        public override DateTime? Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
            => throw new NotSupportedException("SnakeCaseMinuteJsonOptions はレスポンス専用です。");

        /// <inheritdoc/>
        public override void Write(Utf8JsonWriter writer, DateTime? value, JsonSerializerOptions options)
        {
            if (value.HasValue)
            {
                writer.WriteStringValue(ToMinute(value.Value));
            }
            else
            {
                writer.WriteNullValue();
            }
        }
    }
}
