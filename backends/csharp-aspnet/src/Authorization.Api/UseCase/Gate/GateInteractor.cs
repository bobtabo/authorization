/*
 * Gate（JWT 発行・検証）ユースケースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using Authorization.Api.Config;
using Authorization.Api.Domain.Client;
using Authorization.Api.Domain.Gate;
using Authorization.Api.Support;
using Microsoft.IdentityModel.Tokens;

namespace Authorization.Api.UseCase.Gate;

/// <summary>JWT 発行 DTO です。</summary>
public sealed record GateIssueDto(string AccessToken, string MemberId);

/// <summary>JWT 検証 DTO です。</summary>
public sealed record GateVerifyDto(string Identifier, string Token);

/// <summary>Gate ユースケースです。</summary>
public sealed class GateInteractor(
    IClientRepository clientRepo,
    IGateCacheRepository cache,
    JwtSettings jwt,
    IJwtHistoryRepository? historyRepo = null,
    ILogger<GateInteractor>? logger = null)
{
    /// <summary>
    /// アクセストークンに対応するクライアントの秘密鍵で JWT を発行します。
    /// キャッシュ済みであればそれを返します。
    /// </summary>
    /// <param name="dto">クライアントアクセストークンとメンバーID</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>発行（またはキャッシュ済み）のJWT</returns>
    /// <exception cref="AppException">クライアントが存在しない場合（404）</exception>
    public async Task<GateIssueVo> IssueTokenAsync(GateIssueDto dto, CancellationToken ct = default)
    {
        var c = await clientRepo.FindByAccessTokenAsync(dto.AccessToken, ct)
            ?? throw AppException.NotFound("client_not_found");

        var cached = await cache.GetJwtAsync(c.Identifier, dto.MemberId, ct);
        if (!string.IsNullOrWhiteSpace(cached)) return new GateIssueVo(cached);

        var token = IssueJwt(dto.MemberId, c.Identifier, c.PrivateKey, c.Fingerprint, jwt.Issuer, jwt.Ttl);

        try { await cache.PutJwtAsync(c.Identifier, dto.MemberId, token, jwt.CacheTtl, ct); }
        catch (Exception e) { logger?.LogWarning(e, "gate jwt cache put failed"); }

        if (historyRepo is not null)
        {
            try { await historyRepo.SaveAsync(c.Id, dto.MemberId, DateTime.Now, token, ct); }
            catch (Exception e) { logger?.LogWarning(e, "jwt history save failed"); }
        }

        return new GateIssueVo(token);
    }

    /// <summary>クライアントの公開鍵で JWT を検証し、クレームを返します。</summary>
    /// <param name="dto">クライアント識別子とJWT文字列</param>
    /// <param name="ct">キャンセレーショントークン</param>
    /// <returns>JWTのクレーム</returns>
    /// <exception cref="AppException">クライアントが存在しない場合（404）、検証失敗（401）</exception>
    public async Task<GateVerifyVo> VerifyAsync(GateVerifyDto dto, CancellationToken ct = default)
    {
        var c = await clientRepo.FindByIdentifierAsync(dto.Identifier, ct)
            ?? throw AppException.NotFound("client_not_found");
        return new GateVerifyVo(VerifyJwt(dto.Identifier, dto.Token, c.PublicKey, jwt.Issuer));
    }

    /// <summary>RS256 で署名した JWT を発行します。</summary>
    /// <param name="memberId">メンバーID（sub クレームに設定）</param>
    /// <param name="identifier">クライアント識別子（audience に設定）</param>
    /// <param name="privateKeyPem">署名に使うRSA秘密鍵（PEM形式）</param>
    /// <param name="fingerprint">鍵のSHA256フィンガープリント（kid ヘッダーに設定）</param>
    /// <param name="issuer">発行者（issuer に設定）</param>
    /// <param name="ttl">有効期間（秒）</param>
    /// <returns>署名済みJWT文字列</returns>
    public static string IssueJwt(string memberId, string identifier, string privateKeyPem, string fingerprint, string issuer, long ttl)
    {
        var rsa = RSA.Create();
        rsa.ImportFromPem(privateKeyPem);
        var key = new RsaSecurityKey(rsa) { KeyId = fingerprint };
        var now = DateTime.UtcNow;

        var token = new JwtSecurityToken(
            issuer:    issuer,
            audience:  identifier,
            claims:
            [
                new Claim(JwtRegisteredClaimNames.Sub, memberId),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            ],
            notBefore: now,
            expires:   now.AddSeconds(ttl),
            signingCredentials: new SigningCredentials(key, SecurityAlgorithms.RsaSha256)
        );
        token.Payload[JwtRegisteredClaimNames.Iat] = EpochTime.GetIntDate(now);
        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    /// <summary>JWT を検証し、クレームを辞書で返します。</summary>
    /// <param name="identifier">期待するaudience（クライアント識別子）</param>
    /// <param name="tokenStr">検証対象のJWT文字列</param>
    /// <param name="publicKeyPem">検証に使うRSA公開鍵（PEM形式）</param>
    /// <param name="issuer">期待するissuer</param>
    /// <returns>JWTペイロードのクレーム</returns>
    /// <exception cref="AppException">検証失敗（401）</exception>
    public static IReadOnlyDictionary<string, object?> VerifyJwt(string identifier, string tokenStr, string publicKeyPem, string issuer)
    {
        var rsa = RSA.Create();
        rsa.ImportFromPem(publicKeyPem);

        var handler = new JwtSecurityTokenHandler { MapInboundClaims = false };
        var parameters = new TokenValidationParameters
        {
            ValidateIssuer           = true,
            ValidIssuer              = issuer,
            ValidateAudience         = true,
            ValidAudience            = identifier,
            ValidateLifetime         = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey         = new RsaSecurityKey(rsa),
            ValidAlgorithms          = [SecurityAlgorithms.RsaSha256],
            ClockSkew                = TimeSpan.Zero,
        };

        try
        {
            handler.ValidateToken(tokenStr, parameters, out var validated);
            var jwtToken = (JwtSecurityToken)validated;
            return jwtToken.Payload.ToDictionary(kv => kv.Key, kv => (object?)kv.Value);
        }
        catch (SecurityTokenExpiredException)         { throw AppException.Unauthorized("token_expired"); }
        catch (SecurityTokenInvalidIssuerException)   { throw AppException.Unauthorized("invalid_issuer"); }
        catch (SecurityTokenInvalidAudienceException) { throw AppException.Unauthorized("invalid_audience"); }
        catch (Exception e) when (e is SecurityTokenException or ArgumentException)
        {
            throw AppException.Unauthorized("invalid_token");
        }
    }
}
