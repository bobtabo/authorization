/*
 * スタッフ ドメインモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
namespace Authorization.Api.Domain.Staff;

/// <summary>スタッフ権限コードです。</summary>
public static class StaffRole
{
    public const int Admin  = 1;
    public const int Member = 2;

    /// <summary>不正な値は Member に丸めます。</summary>
    /// <param name="value">ロールコード</param>
    /// <returns>ロールコード。Admin以外はMemberに丸める</returns>
    public static int From(int value) => value == Admin ? Admin : Member;

    /// <summary>有効な権限コードかどうか。</summary>
    /// <param name="value">ロールコード</param>
    /// <returns>Admin/Memberのいずれかであればtrue</returns>
    public static bool IsValid(int value) => value is Admin or Member;
}

/// <summary>OAuth プロバイダーコードです。</summary>
public static class StaffProvider
{
    public const int Google = 1;
    public const int Github = 2;
}

/// <summary>スタッフエンティティです。</summary>
public sealed record Staff
{
    public long      Id          { get; init; }
    public string    Name        { get; init; } = "";
    public string    Email       { get; init; } = "";
    public int       Provider    { get; init; }
    public string    ProviderId  { get; init; } = "";
    public string?   Avatar      { get; init; }
    public int       Role        { get; init; } = StaffRole.Member;
    public DateTime? LastLoginAt { get; init; }
    public DateTime  CreatedAt   { get; init; }
    public long?     CreatedBy   { get; init; }
    public DateTime  UpdatedAt   { get; init; }
    public long?     UpdatedBy   { get; init; }
    public DateTime? DeletedAt   { get; init; }
    public long?     DeletedBy   { get; init; }
    public int       Version     { get; init; } = 1;

    /// <summary>状態コード（1=有効、0=無効）。deleted_at の有無で算出します。</summary>
    public int Status => DeletedAt is null ? 1 : 0;
}

/// <summary>スタッフ検索条件です。</summary>
public sealed record StaffCondition
{
    public string?   Keyword  { get; init; }
    public List<int> Roles    { get; init; } = [];
    public int       Offset   { get; init; }
    public int       Limit    { get; init; } = 10;
    public string?   Sort     { get; init; }
    public string?   SortType { get; init; }
}

/// <summary>一覧表示用のスタッフ VO です。</summary>
public sealed record StaffListItem(
    long Id,
    string Name,
    string Email,
    int Role,
    int Status,
    DateTime CreatedAt,
    DateTime UpdatedAt,
    int Version
);

/// <summary>ログイン中スタッフのプロフィール VO です。</summary>
public sealed record StaffVo(long Id, string Name, string? Avatar, int Role);

/// <summary>スタッフリポジトリです。</summary>
public interface IStaffRepository
{
    /// <summary>条件に一致するスタッフの総件数を返します（ページング無視）。</summary>
    /// <param name="cond">検索条件</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>総件数</returns>
    Task<int> CountByConditionAsync(StaffCondition cond, CancellationToken ct = default);

    /// <summary>条件に一致するスタッフ一覧を返します。</summary>
    /// <param name="cond">検索条件</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>スタッフ一覧（ページング適用済み）</returns>
    Task<List<Staff>> FindByConditionAsync(StaffCondition cond, CancellationToken ct = default);

    /// <summary>IDでスタッフを取得します。</summary>
    /// <param name="id">スタッフID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>スタッフ、存在しない場合はnull</returns>
    Task<Staff?> FindByIdAsync(long id, CancellationToken ct = default);

    /// <summary>OAuthプロバイダー情報でスタッフを取得します。</summary>
    /// <param name="provider">OAuthプロバイダーコード</param>
    /// <param name="providerId">プロバイダー側のユーザーID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>スタッフ、存在しない場合はnull</returns>
    Task<Staff?> FindByProviderAsync(int provider, string providerId, CancellationToken ct = default);

    /// <summary>論理削除されていない全スタッフを返します。</summary>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>有効なスタッフ一覧</returns>
    Task<List<Staff>> FindAllActiveAsync(CancellationToken ct = default);

    /// <summary>
    /// 保存します。Id が 0 なら新規登録、それ以外は楽観排他ロック付き更新です。
    /// </summary>
    /// <param name="staff">保存するスタッフ</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>保存後のスタッフ（IDやバージョンが反映済み）</returns>
    /// <exception cref="Support.AppException">バージョン不一致（409）</exception>
    Task<Staff> SaveAsync(Staff staff, CancellationToken ct = default);

    /// <summary>権限を更新します。</summary>
    /// <param name="id">スタッフID</param>
    /// <param name="role">新しい権限コード</param>
    /// <param name="updatedBy">操作を実行したスタッフID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>更新できた場合は true</returns>
    Task<bool> UpdateRoleAsync(long id, int role, long updatedBy, CancellationToken ct = default);

    /// <summary>論理削除します。</summary>
    /// <param name="id">スタッフID</param>
    /// <param name="deletedBy">削除を実行したスタッフID</param>
    /// <param name="version">楽観排他ロック用バージョン番号</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>削除できた場合は true</returns>
    /// <exception cref="Support.AppException">バージョン不一致（409）</exception>
    Task<bool> SoftDeleteAsync(long id, long deletedBy, int version, CancellationToken ct = default);

    /// <summary>論理削除を取り消します。</summary>
    /// <param name="id">スタッフID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>復元できた場合は true</returns>
    Task<bool> RestoreAsync(long id, CancellationToken ct = default);
}
