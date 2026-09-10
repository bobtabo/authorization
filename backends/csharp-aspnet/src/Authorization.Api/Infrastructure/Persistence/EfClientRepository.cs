/*
 * クライアントリポジトリ（EF Core）モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Client;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Infrastructure.Model;
using Authorization.Api.Support;
using Microsoft.EntityFrameworkCore;
using ClientEntity = Authorization.Api.Domain.Client.Client;

namespace Authorization.Api.Infrastructure.Persistence;

/// <summary>EF Core によるクライアントリポジトリです。</summary>
public sealed class EfClientRepository(AppDbContext db) : IClientRepository
{
    /// <summary>検索条件（キーワード・期間・状態）をクエリに適用します（ページング・並び順は含まない）。</summary>
    /// <param name="cond">検索条件</param>
    /// <returns>フィルタ適用済みのクエリ</returns>
    private IQueryable<ClientModel> ApplyFilters(ClientCondition cond)
    {
        var q = db.Clients.AsNoTracking().AsQueryable();
        if (!string.IsNullOrEmpty(cond.Keyword))
        {
            var kw = $"%{cond.Keyword}%";
            q = q.Where(c => EF.Functions.Like(c.Name, kw) || EF.Functions.Like(c.Email, kw));
        }
        if (cond.StartFrom is DateTime from) q = q.Where(c => c.StartAt >= from);
        if (cond.StartTo   is DateTime to)   q = q.Where(c => c.StartAt <= to);
        if (cond.Statuses.Count > 0)         q = q.Where(c => cond.Statuses.Contains(c.Status));
        return q;
    }

    /// <inheritdoc/>
    public async Task<List<ClientEntity>> FindByConditionAsync(ClientCondition cond, CancellationToken ct = default)
    {
        var q   = ApplyFilters(cond);
        var asc = cond.SortType == "asc";
        q = cond.Sort switch
        {
            "name"       => asc ? q.OrderBy(c => c.Name)      : q.OrderByDescending(c => c.Name),
            "status"     => asc ? q.OrderBy(c => c.Status)    : q.OrderByDescending(c => c.Status),
            "updated_at" => asc ? q.OrderBy(c => c.UpdatedAt) : q.OrderByDescending(c => c.UpdatedAt),
            "start_at"   => asc ? q.OrderBy(c => c.StartAt)   : q.OrderByDescending(c => c.StartAt),
            _            => asc ? q.OrderBy(c => c.CreatedAt) : q.OrderByDescending(c => c.CreatedAt),
        };
        if (cond.Limit > 0) q = q.Skip(cond.Offset).Take(Math.Clamp(cond.Limit, 1, 500));
        return (await q.ToListAsync(ct)).Select(ToEntity).ToList();
    }

    /// <inheritdoc/>
    public Task<int> CountByConditionAsync(ClientCondition cond, CancellationToken ct = default) =>
        ApplyFilters(cond).CountAsync(ct);

    /// <inheritdoc/>
    public async Task<ClientEntity?> FindByIdAsync(long id, CancellationToken ct = default)
    {
        var m = await db.Clients.AsNoTracking().FirstOrDefaultAsync(c => c.Id == id, ct);
        return m is null ? null : ToEntity(m);
    }

    /// <inheritdoc/>
    public async Task<ClientEntity?> FindByAccessTokenAsync(string accessToken, CancellationToken ct = default)
    {
        var m = await db.Clients.AsNoTracking().FirstOrDefaultAsync(
            c => c.AccessToken == accessToken && c.Status == ClientStatus.Active && c.DeletedAt == null, ct);
        return m is null ? null : ToEntity(m);
    }

    /// <inheritdoc/>
    public async Task<ClientEntity?> FindByIdentifierAsync(string identifier, CancellationToken ct = default)
    {
        var m = await db.Clients.AsNoTracking()
            .FirstOrDefaultAsync(c => c.Identifier == identifier && c.DeletedAt == null, ct);
        return m is null ? null : ToEntity(m);
    }

    /// <inheritdoc/>
    public async Task<ClientEntity> SaveAsync(ClientEntity c, CancellationToken ct = default)
    {
        if (c.Id == 0)
        {
            var m = new ClientModel
            {
                Name        = c.Name,
                Identifier  = c.Identifier,
                PostCode    = c.PostCode,
                Pref        = c.Pref,
                City        = c.City,
                Address     = c.Address,
                Building    = c.Building,
                Tel         = c.Tel,
                Email       = c.Email,
                AccessToken = c.AccessToken,
                PrivateKey  = c.PrivateKey,
                PublicKey   = c.PublicKey,
                Fingerprint = c.Fingerprint,
                Status      = c.Status,
                StartAt     = c.StartAt,
                StopAt      = c.StopAt,
                CreatedAt   = c.CreatedAt,
                CreatedBy   = (int)(c.CreatedBy ?? 0),
                UpdatedAt   = c.UpdatedAt,
                UpdatedBy   = (int)(c.UpdatedBy ?? 0),
                Version     = c.Version,
            };
            db.Clients.Add(m);
            await db.SaveChangesAsync(ct);
            db.Entry(m).State = EntityState.Detached;
            return c with { Id = m.Id };
        }

        var rows = await db.Clients
            .Where(x => x.Id == c.Id && x.Version == c.Version)
            .ExecuteUpdateAsync(s => s
                .SetProperty(x => x.Name,      c.Name)
                .SetProperty(x => x.PostCode,  c.PostCode)
                .SetProperty(x => x.Pref,      c.Pref)
                .SetProperty(x => x.City,      c.City)
                .SetProperty(x => x.Address,   c.Address)
                .SetProperty(x => x.Building,  c.Building)
                .SetProperty(x => x.Tel,       c.Tel)
                .SetProperty(x => x.Email,     c.Email)
                .SetProperty(x => x.Status,    c.Status)
                .SetProperty(x => x.StartAt,   c.StartAt)
                .SetProperty(x => x.StopAt,    c.StopAt)
                .SetProperty(x => x.UpdatedAt, c.UpdatedAt)
                .SetProperty(x => x.UpdatedBy, (int)(c.UpdatedBy ?? 0))
                .SetProperty(x => x.Version,   c.Version + 1), ct);
        if (rows == 0) throw AppException.Conflict();
        return c with { Version = c.Version + 1 };
    }

    /// <inheritdoc/>
    public async Task SoftDeleteAsync(long id, long deletedBy, int version, CancellationToken ct = default)
    {
        var now  = DateTime.Now;
        var rows = await db.Clients
            .Where(x => x.Id == id && x.Version == version)
            .ExecuteUpdateAsync(s => s
                .SetProperty(x => x.DeletedAt, now)
                .SetProperty(x => x.DeletedBy, (int)deletedBy)
                .SetProperty(x => x.UpdatedAt, now)
                .SetProperty(x => x.UpdatedBy, (int)deletedBy), ct);
        if (rows == 0) throw AppException.Conflict();
    }

    /// <summary>DBモデルをドメインエンティティに変換します。</summary>
    /// <param name="m">DBモデル</param>
    /// <returns>ドメインエンティティ</returns>
    private static ClientEntity ToEntity(ClientModel m) => new()
    {
        Id          = m.Id,
        Name        = m.Name,
        Identifier  = m.Identifier,
        PostCode    = m.PostCode,
        Pref        = m.Pref,
        City        = m.City,
        Address     = m.Address,
        Building    = m.Building ?? "",
        Tel         = m.Tel,
        Email       = m.Email,
        AccessToken = m.AccessToken,
        PrivateKey  = m.PrivateKey,
        PublicKey   = m.PublicKey,
        Fingerprint = m.Fingerprint,
        Status      = m.Status,
        StartAt     = m.StartAt,
        StopAt      = m.StopAt,
        CreatedAt   = m.CreatedAt,
        CreatedBy   = m.CreatedBy,
        UpdatedAt   = m.UpdatedAt,
        UpdatedBy   = m.UpdatedBy,
        DeletedAt   = m.DeletedAt,
        DeletedBy   = m.DeletedBy,
        Version     = m.Version,
    };
}

/// <summary>EF Core による JWT 履歴リポジトリです。</summary>
public sealed class EfJwtHistoryRepository(AppDbContext db) : IJwtHistoryRepository
{
    /// <summary>指定クライアントの未削除JWT履歴クエリを組み立てます。</summary>
    /// <param name="clientId">クライアントID</param>
    /// <returns>クエリ</returns>
    private IQueryable<JwtHistoryModel> Base(long clientId) =>
        db.JwtHistories.AsNoTracking().Where(h => h.ClientId == clientId && h.DeletedAt == null);

    /// <inheritdoc/>
    public Task<int> CountByConditionAsync(JwtHistoryCondition cond, CancellationToken ct = default) =>
        Base(cond.ClientId).CountAsync(ct);

    /// <inheritdoc/>
    public async Task<List<JwtHistory>> FindByConditionAsync(JwtHistoryCondition cond, CancellationToken ct = default)
    {
        var q   = Base(cond.ClientId);
        var asc = string.Equals(cond.SortType, "asc", StringComparison.OrdinalIgnoreCase);
        q = cond.Sort == "member_id"
            ? (asc ? q.OrderBy(h => h.MemberId) : q.OrderByDescending(h => h.MemberId))
            : (asc ? q.OrderBy(h => h.IssueAt)  : q.OrderByDescending(h => h.IssueAt));
        q = q.Skip(cond.Offset).Take(Math.Clamp(cond.Limit, 1, 500));
        return (await q.ToListAsync(ct))
            .Select(h => new JwtHistory(h.Id, h.ClientId, h.MemberId, h.IssueAt, h.Jwt, h.CreatedAt, h.DeletedAt))
            .ToList();
    }

    /// <inheritdoc/>
    public async Task SaveAsync(long clientId, string memberId, DateTime issueAt, string jwt, CancellationToken ct = default)
    {
        var now = DateTime.Now;
        var m = new JwtHistoryModel
        {
            ClientId  = clientId,
            MemberId  = memberId,
            IssueAt   = issueAt,
            Jwt       = jwt,
            CreatedAt = now,
            CreatedBy = 0,
            UpdatedAt = now,
            UpdatedBy = 0,
            Version   = 1,
        };
        db.JwtHistories.Add(m);
        await db.SaveChangesAsync(ct);
        db.Entry(m).State = EntityState.Detached;
    }
}
