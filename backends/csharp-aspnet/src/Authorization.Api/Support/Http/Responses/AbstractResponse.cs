/*
 * 基底Responseモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Reflection;
using System.Text.Json;

namespace Authorization.Api.Support.Http.Responses;

/// <summary>
/// 基底Responseクラスです（PHP版 App\Support\Http\Responses\AbstractResponse 相当）。
/// 派生クラスのパブリックプロパティを1件ずつ辞書に書き出すコードを避けるため、
/// リフレクションでプロパティ名を snake_case に変換して <see cref="Attributes"/> を組み立てます。
/// </summary>
public abstract class AbstractResponse
{
    /// <summary>出力対象のプロパティ一覧です（派生クラスのプロパティのみ、宣言順）。</summary>
    private readonly PropertyInfo[] properties = [];

    /// <summary>コンストラクタ。</summary>
    protected AbstractResponse()
    {
        properties = GetType().GetProperties(BindingFlags.Public | BindingFlags.Instance)
            .Where(p => !ExcludeKeys.Contains(p.Name))
            .ToArray();
    }

    /// <summary>出力から除外するプロパティ名です。既定では除外しません。</summary>
    protected virtual IReadOnlySet<string> ExcludeKeys => ExcludeKeysDefault;

    private static readonly HashSet<string> ExcludeKeysDefault = [];

    /// <summary>
    /// パブリックプロパティを snake_case キーの辞書に変換します。
    /// <c>Results.Json(...)</c> にそのまま渡せます。
    /// </summary>
    /// <returns>JSON化用の辞書</returns>
    public Dictionary<string, object?> Attributes()
    {
        var result = new Dictionary<string, object?>();
        foreach (var property in properties)
        {
            result[JsonNamingPolicy.SnakeCaseLower.ConvertName(property.Name)] = property.GetValue(this);
        }
        return result;
    }
}
