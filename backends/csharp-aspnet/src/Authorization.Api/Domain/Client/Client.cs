/*
 * クライアント ドメインモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Domain.Client;

/// <summary>クライアント状態コードです。</summary>
public static class ClientStatus
{
    public const int Inactive  = 1;
    public const int Active    = 2;
    public const int Suspended = 3;
    public const int Closed    = 4;
}

/// <summary>クライアントエンティティです。</summary>
public sealed record Client
{
    public long      Id          { get; init; }
    public string    Name        { get; init; } = "";
    public string    Identifier  { get; init; } = "";
    public string    PostCode    { get; init; } = "";
    public string    Pref        { get; init; } = "";
    public string    City        { get; init; } = "";
    public string    Address     { get; init; } = "";
    public string    Building    { get; init; } = "";
    public string    Tel         { get; init; } = "";
    public string    Email       { get; init; } = "";
    public string    AccessToken { get; init; } = "";
    public string    PrivateKey  { get; init; } = "";
    public string    PublicKey   { get; init; } = "";
    public string    Fingerprint { get; init; } = "";
    public int       Status      { get; init; } = ClientStatus.Inactive;
    public DateTime? StartAt     { get; init; }
    public DateTime? StopAt      { get; init; }
    public DateTime  CreatedAt   { get; init; }
    public long?     CreatedBy   { get; init; }
    public DateTime  UpdatedAt   { get; init; }
    public long?     UpdatedBy   { get; init; }
    public DateTime? DeletedAt   { get; init; }
    public long?     DeletedBy   { get; init; }
    public int       Version     { get; init; } = 1;
}

/// <summary>クライアント検索条件です。</summary>
public sealed record ClientCondition
{
    public string?    Keyword   { get; init; }
    public DateTime?  StartFrom { get; init; }
    public DateTime?  StartTo   { get; init; }
    public List<int>  Statuses  { get; init; } = [];
    public int        Offset    { get; init; }
    public int        Limit     { get; init; } = 10;
    public string?    Sort      { get; init; }
    public string?    SortType  { get; init; }
}

/// <summary>一覧表示用のクライアント VO です。</summary>
public sealed record ClientListItem(
    long Id,
    string Name,
    int Status,
    DateTime? StartAt,
    DateTime? StopAt,
    DateTime CreatedAt,
    DateTime UpdatedAt
);

/// <summary>詳細表示用のクライアント VO です。</summary>
public sealed record ClientDetailVo(
    long Id,
    string Name,
    string Identifier,
    string PostCode,
    string Pref,
    string City,
    string Address,
    string Building,
    string Tel,
    string Email,
    int Status,
    DateTime? StartAt,
    DateTime? StopAt,
    DateTime CreatedAt,
    DateTime UpdatedAt,
    int Version
);

/// <summary>登録結果 VO です。</summary>
public sealed record ClientStoreResultVo(long Id, string Name, string Identifier, string Email, string AccessToken);

/// <summary>QR コード用 VO です。</summary>
public sealed record ClientQrVo(string Identifier, string DeeplinkUrl);

/// <summary>スマホアプリ向け情報 VO です。</summary>
public sealed record ClientInfoVo(string Identifier, string Name, int Status);

/// <summary>利用開始結果 VO です。</summary>
public sealed record ClientStartVo(string AccessToken);

/// <summary>クライアントリポジトリです。</summary>
public interface IClientRepository
{
    /// <summary>条件に一致するクライアント一覧を返します。</summary>
    /// <param name="cond">検索条件</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クライアント一覧（ページング適用済み）</returns>
    Task<List<Client>> FindByConditionAsync(ClientCondition cond, CancellationToken ct = default);

    /// <summary>条件に一致するクライアントの総件数を返します（ページング無視）。</summary>
    /// <param name="cond">検索条件</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>総件数</returns>
    Task<int> CountByConditionAsync(ClientCondition cond, CancellationToken ct = default);

    /// <summary>IDでクライアントを取得します。</summary>
    /// <param name="id">クライアントID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クライアント、存在しない場合はnull</returns>
    Task<Client?> FindByIdAsync(long id, CancellationToken ct = default);

    /// <summary>アクセストークンで有効なクライアントを取得します。</summary>
    /// <param name="accessToken">アクセストークン</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クライアント（状態がActiveかつ未削除のもの）、存在しない場合はnull</returns>
    Task<Client?> FindByAccessTokenAsync(string accessToken, CancellationToken ct = default);

    /// <summary>識別子でクライアントを取得します。</summary>
    /// <param name="identifier">クライアント識別子</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>クライアント、存在しない場合はnull</returns>
    Task<Client?> FindByIdentifierAsync(string identifier, CancellationToken ct = default);

    /// <summary>
    /// 保存します。Id が 0 なら新規登録、それ以外は楽観排他ロック付き更新です。
    /// </summary>
    /// <param name="client">保存するクライアント</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>保存後のクライアント（IDやバージョンが反映済み）</returns>
    /// <exception cref="Support.AppException">バージョン不一致（409）</exception>
    Task<Client> SaveAsync(Client client, CancellationToken ct = default);

    /// <summary>論理削除します。</summary>
    /// <param name="id">クライアントID</param>
    /// <param name="deletedBy">削除を実行したスタッフID</param>
    /// <param name="version">楽観排他ロック用バージョン番号</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <exception cref="Support.AppException">バージョン不一致（409）</exception>
    Task SoftDeleteAsync(long id, long deletedBy, int version, CancellationToken ct = default);
}

/// <summary>JWT 履歴エンティティです。</summary>
public sealed record JwtHistory(
    long Id,
    long ClientId,
    string MemberId,
    DateTime IssueAt,
    string Jwt,
    DateTime CreatedAt,
    DateTime? DeletedAt
);

/// <summary>JWT 履歴検索条件です。</summary>
public sealed record JwtHistoryCondition(
    long ClientId,
    int Offset = 0,
    int Limit = 10,
    string Sort = "issue_at",
    string SortType = "desc"
);

/// <summary>JWT 履歴リポジトリです。</summary>
public interface IJwtHistoryRepository
{
    /// <summary>条件に一致するJWT履歴の総件数を返します（ページング無視）。</summary>
    /// <param name="cond">検索条件</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>総件数</returns>
    Task<int> CountByConditionAsync(JwtHistoryCondition cond, CancellationToken ct = default);

    /// <summary>条件に一致するJWT履歴一覧を返します。</summary>
    /// <param name="cond">検索条件</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>JWT履歴一覧（ページング適用済み）</returns>
    Task<List<JwtHistory>> FindByConditionAsync(JwtHistoryCondition cond, CancellationToken ct = default);

    /// <summary>JWT発行履歴を1件保存します。</summary>
    /// <param name="clientId">クライアントID</param>
    /// <param name="memberId">メンバーID</param>
    /// <param name="issueAt">発行日時</param>
    /// <param name="jwt">発行したJWT文字列</param>
    /// <param name="ct">キャンセレーショントークン</param>
    Task SaveAsync(long clientId, string memberId, DateTime issueAt, string jwt, CancellationToken ct = default);
}
