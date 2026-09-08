/*
 * クライアントユースケースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.Security.Cryptography;
using Authorization.Api.Domain.Client;
using Authorization.Api.Infrastructure.Db;
using Authorization.Api.Support;
using ClientEntity = Authorization.Api.Domain.Client.Client;

namespace Authorization.Api.UseCase.Client;

/// <summary>クライアントユースケースです。</summary>
public sealed class ClientInteractor(IClientRepository repo, AppDbContext db)
{
    /// <summary>条件に一致するクライアント一覧と総件数を返します。</summary>
    public async Task<(List<ClientListItem> Items, int Count)> FindByConditionWithCountAsync(
        ClientListConditionDto dto, CancellationToken ct = default)
    {
        var cond = new ClientCondition
        {
            Keyword   = dto.Keyword,
            StartFrom = DateFormat.ParseDate(dto.StartFrom),
            StartTo   = DateFormat.ParseDate(dto.StartTo),
            Statuses  = dto.Statuses?.ToList() ?? [],
            Offset    = dto.Offset,
            Limit     = dto.Limit,
            Sort      = dto.Sort,
            SortType  = dto.SortType,
        };
        var count = await repo.CountByConditionAsync(cond, ct);
        var items = (await repo.FindByConditionAsync(cond, ct))
            .Select(c => new ClientListItem(c.Id, c.Name, c.Status, c.StartAt, c.StopAt, c.CreatedAt, c.UpdatedAt))
            .ToList();
        return (items, count);
    }

    /// <summary>クライアント詳細を取得します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<ClientDetailVo> FindByIdAsync(long id, CancellationToken ct = default)
    {
        var c = await repo.FindByIdAsync(id, ct) ?? throw AppException.NotFound("client_not_found");
        return ToDetail(c);
    }

    /// <summary>
    /// クライアントを登録します。RSA 4096bit 鍵ペア・アクセストークン・識別子を生成し、
    /// 状態は Inactive で保存します。
    /// </summary>
    public async Task<ClientStoreResultVo> StoreAsync(ClientStoreDto dto, CancellationToken ct = default)
    {
        var (privPem, pubPem, fingerprint) = GenerateRsaKeys();
        var now = DateTime.Now;
        var c = new ClientEntity
        {
            Name        = dto.Name,
            Identifier  = GenerateHex(8),
            PostCode    = dto.PostCode,
            Pref        = dto.Pref,
            City        = dto.City,
            Address     = dto.Address,
            Building    = dto.Building,
            Tel         = dto.Tel,
            Email       = dto.Email,
            AccessToken = GenerateHex(32),
            PrivateKey  = privPem,
            PublicKey   = pubPem,
            Fingerprint = fingerprint,
            Status      = ClientStatus.Inactive,
            CreatedAt   = now,
            CreatedBy   = dto.ExecutorId,
            UpdatedAt   = now,
            UpdatedBy   = dto.ExecutorId,
        };
        var saved = await repo.SaveAsync(c, ct);
        return new ClientStoreResultVo(saved.Id, saved.Name, saved.Identifier, saved.Email, saved.AccessToken);
    }

    /// <summary>
    /// クライアントを更新します（楽観排他ロック）。状態が Active になる場合は start_at を、
    /// Suspended になる場合は stop_at を設定します。
    /// </summary>
    /// <exception cref="AppException">存在しない場合（404）、バージョン不一致（409）</exception>
    public async Task<ClientDetailVo> UpdateAsync(ClientUpdateDto dto, CancellationToken ct = default)
    {
        var c = await repo.FindByIdAsync(dto.Id, ct) ?? throw AppException.NotFound("client_not_found");
        if (c.Version != dto.Version) throw AppException.Conflict();

        if (dto.Name     is not null) c = c with { Name     = dto.Name };
        if (dto.PostCode is not null) c = c with { PostCode = dto.PostCode };
        if (dto.Pref     is not null) c = c with { Pref     = dto.Pref };
        if (dto.City     is not null) c = c with { City     = dto.City };
        if (dto.Address  is not null) c = c with { Address  = dto.Address };
        if (dto.Building is not null) c = c with { Building = dto.Building };
        if (dto.Tel      is not null) c = c with { Tel      = dto.Tel };
        if (dto.Email    is not null) c = c with { Email    = dto.Email };

        var now = DateTime.Now;
        if (dto.Status is int status)
        {
            c = status switch
            {
                ClientStatus.Active    => c with { Status = status, StartAt = c.StartAt ?? now, StopAt = null },
                ClientStatus.Suspended => c with { Status = status, StopAt = now },
                _                      => c with { Status = status },
            };
        }

        c = c with { UpdatedAt = now, UpdatedBy = dto.ExecutorId };
        return ToDetail(await repo.SaveAsync(c, ct));
    }

    /// <summary>
    /// クライアントを論理削除します。状態を Closed に更新した後、deleted_at を設定します。
    /// </summary>
    /// <exception cref="AppException">バージョン未指定（400）、存在しない場合（404）、バージョン不一致（409）</exception>
    public async Task DestroyAsync(long id, long executorId, int? version, CancellationToken ct = default)
    {
        if (version is not int v) throw AppException.BadRequest("version_required");

        var c = await repo.FindByIdAsync(id, ct) ?? throw AppException.NotFound("client_not_found");
        if (c.Version != v) throw AppException.Conflict();

        await using var tx = await db.Database.BeginTransactionAsync(ct);
        var now   = DateTime.Now;
        var saved = await repo.SaveAsync(c with { Status = ClientStatus.Closed, UpdatedAt = now, UpdatedBy = executorId }, ct);
        await repo.SoftDeleteAsync(id, executorId, saved.Version, ct);
        await tx.CommitAsync(ct);
    }

    /// <summary>QR コード用データを返します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<ClientQrVo> GetQrAsync(ClientQrDto dto, CancellationToken ct = default)
    {
        var c = await repo.FindByIdentifierAsync(dto.Identifier, ct) ?? throw AppException.NotFound("client_not_found");
        return new ClientQrVo(c.Identifier, $"authgateway://clients/{c.Identifier}/info");
    }

    /// <summary>スマホアプリ向けクライアント情報を返します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<ClientInfoVo> GetInfoAsync(ClientInfoDto dto, CancellationToken ct = default)
    {
        var c = await repo.FindByIdentifierAsync(dto.Identifier, ct) ?? throw AppException.NotFound("client_not_found");
        return new ClientInfoVo(c.Identifier, c.Name, c.Status);
    }

    /// <summary>利用開始します。Active 以外なら Active に遷移し、アクセストークンを返します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task<ClientStartVo> StartAsync(ClientStartDto dto, CancellationToken ct = default)
    {
        var c = await repo.FindByIdentifierAsync(dto.Identifier, ct) ?? throw AppException.NotFound("client_not_found");
        if (c.Status != ClientStatus.Active)
        {
            var now = DateTime.Now;
            c = await repo.SaveAsync(c with
            {
                Status    = ClientStatus.Active,
                StartAt   = c.StartAt ?? now,
                StopAt    = null,
                UpdatedAt = now,
                UpdatedBy = 0,
            }, ct);
        }
        return new ClientStartVo(c.AccessToken);
    }

    /// <summary>利用停止します。Active の場合のみ Suspended に遷移します。</summary>
    /// <exception cref="AppException">存在しない場合（404）</exception>
    public async Task StopAsync(ClientStopDto dto, CancellationToken ct = default)
    {
        var c = await repo.FindByIdentifierAsync(dto.Identifier, ct) ?? throw AppException.NotFound("client_not_found");
        if (c.Status == ClientStatus.Active)
        {
            var now = DateTime.Now;
            await repo.SaveAsync(c with { Status = ClientStatus.Suspended, StopAt = now, UpdatedAt = now, UpdatedBy = 0 }, ct);
        }
    }

    private static ClientDetailVo ToDetail(ClientEntity c) => new(
        c.Id, c.Name, c.Identifier, c.PostCode, c.Pref, c.City, c.Address, c.Building, c.Tel, c.Email,
        c.Status, c.StartAt, c.StopAt, c.CreatedAt, c.UpdatedAt, c.Version);

    /// <summary>暗号論的乱数から 16 進文字列を生成します。</summary>
    public static string GenerateHex(int byteCount) =>
        Convert.ToHexStringLower(RandomNumberGenerator.GetBytes(byteCount));

    /// <summary>
    /// RSA 4096bit 鍵ペアを生成し、(PKCS#8 秘密鍵 PEM, SubjectPublicKeyInfo 公開鍵 PEM, SHA256 フィンガープリント) を返します。
    /// </summary>
    public static (string PrivatePem, string PublicPem, string Fingerprint) GenerateRsaKeys()
    {
        using var rsa = RSA.Create(4096);
        var pub         = rsa.ExportSubjectPublicKeyInfo();
        var privPem     = PemEncoding.WriteString("PRIVATE KEY", rsa.ExportPkcs8PrivateKey()) + "\n";
        var pubPem      = PemEncoding.WriteString("PUBLIC KEY", pub) + "\n";
        var fingerprint = "SHA256:" + Convert.ToBase64String(SHA256.HashData(pub)).TrimEnd('=');
        return (privPem, pubPem, fingerprint);
    }
}
