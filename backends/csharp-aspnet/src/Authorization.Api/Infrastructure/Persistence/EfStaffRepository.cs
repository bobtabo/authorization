/*
 * スタッフリポジトリ（EF Core）モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using Authorization.Api.Domain.Staff;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Infrastructure.Model;
using Authorization.Api.Support;
using Microsoft.EntityFrameworkCore;
using StaffEntity = Authorization.Api.Domain.Staff.Staff;

namespace Authorization.Api.Infrastructure.Persistence;

/// <summary>EF Core によるスタッフリポジトリです。</summary>
public sealed class EfStaffRepository(AppDbContext db) : IStaffRepository
{
    private IQueryable<StaffModel> ApplyFilters(StaffCondition cond)
    {
        var q = db.Staffs.AsNoTracking().AsQueryable();
        if (!string.IsNullOrEmpty(cond.Keyword))
        {
            var kw = $"%{cond.Keyword}%";
            q = q.Where(s => EF.Functions.Like(s.Name, kw) || EF.Functions.Like(s.Email, kw));
        }
        if (cond.Roles.Count > 0) q = q.Where(s => cond.Roles.Contains(s.Role));
        return q;
    }

    public Task<int> CountByConditionAsync(StaffCondition cond, CancellationToken ct = default) =>
        ApplyFilters(cond).CountAsync(ct);

    public async Task<List<StaffEntity>> FindByConditionAsync(StaffCondition cond, CancellationToken ct = default)
    {
        var q    = ApplyFilters(cond);
        var desc = cond.SortType == "desc";
        q = cond.Sort switch
        {
            "name"       => desc ? q.OrderByDescending(s => s.Name)      : q.OrderBy(s => s.Name),
            "role"       => desc ? q.OrderByDescending(s => s.Role)      : q.OrderBy(s => s.Role),
            "created_at" => desc ? q.OrderByDescending(s => s.CreatedAt) : q.OrderBy(s => s.CreatedAt),
            _            => desc ? q.OrderByDescending(s => s.Id)        : q.OrderBy(s => s.Id),
        };
        q = q.Skip(cond.Offset).Take(Math.Clamp(cond.Limit, 1, 500));
        return (await q.ToListAsync(ct)).Select(ToEntity).ToList();
    }

    public async Task<StaffEntity?> FindByIdAsync(long id, CancellationToken ct = default)
    {
        var m = await db.Staffs.AsNoTracking().FirstOrDefaultAsync(s => s.Id == id, ct);
        return m is null ? null : ToEntity(m);
    }

    public async Task<StaffEntity?> FindByProviderAsync(int provider, string providerId, CancellationToken ct = default)
    {
        var m = await db.Staffs.AsNoTracking()
            .FirstOrDefaultAsync(s => s.Provider == provider && s.ProviderId == providerId, ct);
        return m is null ? null : ToEntity(m);
    }

    public async Task<List<StaffEntity>> FindAllActiveAsync(CancellationToken ct = default) =>
        (await db.Staffs.AsNoTracking()
            .Where(s => s.DeletedAt == null)
            .OrderByDescending(s => s.CreatedAt)
            .ToListAsync(ct))
        .Select(ToEntity).ToList();

    public async Task<StaffEntity> SaveAsync(StaffEntity s, CancellationToken ct = default)
    {
        if (s.Id == 0)
        {
            var m = new StaffModel
            {
                Name        = s.Name,
                Email       = s.Email,
                Provider    = s.Provider,
                ProviderId  = s.ProviderId,
                Avatar      = s.Avatar,
                Role        = s.Role,
                LastLoginAt = s.LastLoginAt,
                CreatedAt   = s.CreatedAt,
                CreatedBy   = (int)(s.CreatedBy ?? 0),
                UpdatedAt   = s.UpdatedAt,
                UpdatedBy   = (int)(s.UpdatedBy ?? 0),
                Version     = s.Version,
            };
            db.Staffs.Add(m);
            await db.SaveChangesAsync(ct);
            db.Entry(m).State = EntityState.Detached;
            return s with { Id = m.Id };
        }

        var updatedBy = s.UpdatedBy is long ub ? (int?)ub : null;
        var rows = await db.Staffs
            .Where(x => x.Id == s.Id && x.Version == s.Version)
            .ExecuteUpdateAsync(u => u
                .SetProperty(x => x.Name,        s.Name)
                .SetProperty(x => x.Email,       s.Email)
                .SetProperty(x => x.Avatar,      s.Avatar)
                .SetProperty(x => x.Role,        s.Role)
                .SetProperty(x => x.LastLoginAt, s.LastLoginAt)
                .SetProperty(x => x.UpdatedAt,   s.UpdatedAt)
                .SetProperty(x => x.UpdatedBy,   updatedBy)
                .SetProperty(x => x.Version,     s.Version + 1), ct);
        if (rows == 0) throw AppException.Conflict();
        return s with { Version = s.Version + 1 };
    }

    public async Task<bool> UpdateRoleAsync(long id, int role, long updatedBy, CancellationToken ct = default)
    {
        var now  = DateTime.Now;
        var rows = await db.Staffs.Where(x => x.Id == id).ExecuteUpdateAsync(u => u
            .SetProperty(x => x.Role,      role)
            .SetProperty(x => x.UpdatedAt, now)
            .SetProperty(x => x.UpdatedBy, (int)updatedBy), ct);
        return rows > 0;
    }

    public async Task<bool> SoftDeleteAsync(long id, long deletedBy, int version, CancellationToken ct = default)
    {
        var now  = DateTime.Now;
        var rows = await db.Staffs.Where(x => x.Id == id && x.Version == version).ExecuteUpdateAsync(u => u
            .SetProperty(x => x.DeletedAt, now)
            .SetProperty(x => x.DeletedBy, (int)deletedBy)
            .SetProperty(x => x.UpdatedAt, now)
            .SetProperty(x => x.UpdatedBy, (int)deletedBy), ct);
        if (rows == 0) throw AppException.Conflict();
        return true;
    }

    public async Task<bool> RestoreAsync(long id, CancellationToken ct = default)
    {
        var now  = DateTime.Now;
        var rows = await db.Staffs.Where(x => x.Id == id).ExecuteUpdateAsync(u => u
            .SetProperty(x => x.DeletedAt, (DateTime?)null)
            .SetProperty(x => x.DeletedBy, (int?)null)
            .SetProperty(x => x.UpdatedAt, now), ct);
        return rows > 0;
    }

    private static StaffEntity ToEntity(StaffModel m) => new()
    {
        Id          = m.Id,
        Name        = m.Name,
        Email       = m.Email,
        Provider    = m.Provider,
        ProviderId  = m.ProviderId,
        Avatar      = m.Avatar,
        Role        = m.Role,
        LastLoginAt = m.LastLoginAt,
        CreatedAt   = m.CreatedAt,
        CreatedBy   = m.CreatedBy,
        UpdatedAt   = m.UpdatedAt,
        UpdatedBy   = m.UpdatedBy,
        DeletedAt   = m.DeletedAt,
        DeletedBy   = m.DeletedBy,
        Version     = m.Version,
    };
}
