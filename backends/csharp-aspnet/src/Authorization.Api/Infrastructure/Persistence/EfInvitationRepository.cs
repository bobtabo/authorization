/*
 * 招待リポジトリ（EF Core）モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Security.Cryptography;
using Authorization.Api.Config;
using Authorization.Api.Domain.Invitation;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Infrastructure.Model;
using Microsoft.EntityFrameworkCore;

namespace Authorization.Api.Infrastructure.Persistence;

/// <summary>EF Core による招待リポジトリです。</summary>
public sealed class EfInvitationRepository(AppDbContext db, AppSettings app) : IInvitationRepository
{
    /// <inheritdoc/>
    public async Task<InvitationVo?> GetCurrentByRoleAsync(int role, CancellationToken ct = default)
    {
        var m = await db.Invitations.AsNoTracking()
            .Where(i => i.Role == role && i.DeletedAt == null)
            .OrderByDescending(i => i.CreatedAt)
            .FirstOrDefaultAsync(ct);
        return m is null ? null : BuildVo(m.Token, m.Role, app.FrontendUrl);
    }

    /// <inheritdoc/>
    public async Task<InvitationVo> IssueAsync(int role, CancellationToken ct = default)
    {
        var now   = DateTime.Now;
        var token = Convert.ToHexStringLower(RandomNumberGenerator.GetBytes(16));
        var m = new InvitationModel
        {
            Token     = token,
            Role      = role,
            CreatedAt = now,
            CreatedBy = 0,
            UpdatedAt = now,
            UpdatedBy = 0,
        };
        db.Invitations.Add(m);
        await db.SaveChangesAsync(ct);
        db.Entry(m).State = EntityState.Detached;
        return BuildVo(token, role, app.FrontendUrl);
    }

    /// <inheritdoc/>
    public async Task<InvitationVo?> FindByTokenAsync(string token, CancellationToken ct = default)
    {
        var m = await db.Invitations.AsNoTracking()
            .FirstOrDefaultAsync(i => i.Token == token && i.DeletedAt == null, ct);
        return m is null ? null : BuildVo(m.Token, m.Role, app.FrontendUrl);
    }

    /// <summary>招待 URL と表示用 URL を組み立てます。</summary>
    /// <param name="token">招待トークン</param>
    /// <param name="role">ロール種別（1=管理者、2=メンバー）</param>
    /// <param name="frontendUrl">フロントエンドのベースURL</param>
    /// <returns>招待</returns>
    public static InvitationVo BuildVo(string token, int role, string frontendUrl)
    {
        var url = $"{frontendUrl}/invitation/{token}";
        return new InvitationVo(token, role, url, BuildDisplayUrl(url));
    }

    /// <summary>トークン部分を省略した表示用 URL を返します。</summary>
    /// <param name="url">招待URL</param>
    /// <returns>トークン部分を省略した表示用URL</returns>
    public static string BuildDisplayUrl(string url)
    {
        const string seg = "/invitation/";
        var idx = url.IndexOf(seg, StringComparison.Ordinal);
        if (idx != -1)
        {
            var baseUrl = url[..(idx + seg.Length)];
            var after   = url[(idx + seg.Length)..];
            var tokEnd  = after.IndexOfAny(['?', '#']);
            if (tokEnd < 0) tokEnd = after.Length;
            var tok    = after[..tokEnd];
            var suffix = after[tokEnd..];
            if (tok.Length > 13) return $"{baseUrl}{tok[..6]}...{tok[^4..]}{suffix}";
        }
        return url.Length > 72 ? url[..68] + "..." : url;
    }
}
