/*
 * ページャー算出モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Support;

/// <summary>一覧 API のページャー情報です（openapi.yml の Pager スキーマに対応）。</summary>
public sealed record Pager(
    int Count,
    int Limit,
    bool Next,
    bool Previous,
    int Page,
    int NextPage,
    int PreviousPage,
    int PageCount,
    bool First,
    bool Last,
    int FirstRecordCount,
    int LastRecordCount,
    int StartPage,
    int EndPage
)
{
    private const int DefaultPageCount = 5;

    /// <summary>
    /// ページャー情報を組み立てます。
    /// </summary>
    /// <param name="count">総件数</param>
    /// <param name="limit">取得件数</param>
    /// <param name="offset">オフセット</param>
    /// <param name="recordCount">現在ページの件数</param>
    public static Pager Build(int count, int limit, int offset, int recordCount)
    {
        var effectiveLimit = limit <= 0 ? 10 : limit;
        var pageCount = Math.Max(1, (int)Math.Ceiling(count / (double)effectiveLimit));
        var page      = (int)Math.Ceiling(offset / (double)effectiveLimit) + 1;
        var startPage = Math.Max(1, page - (DefaultPageCount - 1));
        var endPage   = Math.Min(pageCount, startPage + (DefaultPageCount - 1));
        var firstRecordCount = count == 0 || recordCount == 0 ? 0 : offset + 1;
        var lastRecordCount  = recordCount == 0 ? 0 : offset + recordCount;

        return new Pager(
            Count:            count,
            Limit:            effectiveLimit,
            Next:             pageCount > page,
            Previous:         page > 1,
            Page:             page,
            NextPage:         page + 1,
            PreviousPage:     page - 1,
            PageCount:        pageCount,
            First:            page > 1,
            Last:             pageCount > page,
            FirstRecordCount: firstRecordCount,
            LastRecordCount:  lastRecordCount,
            StartPage:        startPage,
            EndPage:          endPage
        );
    }

    /// <summary>JSON レスポンス用のオブジェクトへ変換します（キーは camelCase）。</summary>
    public Dictionary<string, object> ToJson() => new()
    {
        ["count"]            = Count,
        ["limit"]            = Limit,
        ["next"]             = Next,
        ["previous"]         = Previous,
        ["page"]             = Page,
        ["nextPage"]         = NextPage,
        ["previousPage"]     = PreviousPage,
        ["pageCount"]        = PageCount,
        ["first"]            = First,
        ["last"]             = Last,
        ["firstRecordCount"] = FirstRecordCount,
        ["lastRecordCount"]  = LastRecordCount,
        ["startPage"]        = StartPage,
        ["endPage"]          = EndPage,
    };
}
