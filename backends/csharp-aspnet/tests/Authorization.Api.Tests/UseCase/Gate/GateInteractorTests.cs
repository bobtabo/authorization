using Authorization.Api.Config;
using Authorization.Api.Domain.Client;
using Authorization.Api.Support;
using Authorization.Api.UseCase.Client;
using Authorization.Api.UseCase.Gate;
using ClientEntity = Authorization.Api.Domain.Client.Client;

namespace Authorization.Api.Tests.UseCase.Gate;

public class GateInteractorTests
{
    private static readonly JwtSettings Jwt = new(Issuer: "authorization", Algorithm: "RS256", Ttl: 1800, CacheTtl: 1800);

    private static ClientEntity MakeClient(long id = 1, string identifier = "client-1", string accessToken = "token-1",
        int status = ClientStatus.Active, DateTime? deletedAt = null)
    {
        var (priv, pub, fp) = ClientInteractor.GenerateRsaKeys();
        return new ClientEntity
        {
            Id          = id,
            Identifier  = identifier,
            AccessToken = accessToken,
            Status      = status,
            DeletedAt   = deletedAt,
            PrivateKey  = priv,
            PublicKey   = pub,
            Fingerprint = fp,
        };
    }

    // ── IssueJwt / VerifyJwt（静的メソッド）のラウンドトリップ ──

    [Fact]
    public void IssueJwt_ThenVerifyJwt_RoundTripsSuccessfully()
    {
        var (priv, pub, fp) = ClientInteractor.GenerateRsaKeys();

        var token  = GateInteractor.IssueJwt("member-1", "client-1", priv, fp, "authorization", 1800);
        var claims = GateInteractor.VerifyJwt("client-1", token, pub, "authorization");

        Assert.Equal("member-1", claims["sub"]);
    }

    [Fact]
    public void VerifyJwt_WrongIssuer_ThrowsUnauthorized()
    {
        var (priv, pub, fp) = ClientInteractor.GenerateRsaKeys();
        var token = GateInteractor.IssueJwt("member-1", "client-1", priv, fp, "authorization", 1800);

        var ex = Assert.Throws<AppException>(() => GateInteractor.VerifyJwt("client-1", token, pub, "other-issuer"));

        Assert.Equal(401, ex.StatusCode);
    }

    [Fact]
    public void VerifyJwt_WrongAudience_ThrowsUnauthorized()
    {
        var (priv, pub, fp) = ClientInteractor.GenerateRsaKeys();
        var token = GateInteractor.IssueJwt("member-1", "client-1", priv, fp, "authorization", 1800);

        var ex = Assert.Throws<AppException>(() => GateInteractor.VerifyJwt("other-client", token, pub, "authorization"));

        Assert.Equal(401, ex.StatusCode);
    }

    [Fact]
    public async Task VerifyJwt_Expired_ThrowsUnauthorized()
    {
        var (priv, pub, fp) = ClientInteractor.GenerateRsaKeys();
        var token = GateInteractor.IssueJwt("member-1", "client-1", priv, fp, "authorization", ttl: 1);
        await Task.Delay(1100);

        var ex = Assert.Throws<AppException>(() => GateInteractor.VerifyJwt("client-1", token, pub, "authorization"));

        Assert.Equal(401, ex.StatusCode);
    }

    [Fact]
    public void VerifyJwt_WrongPublicKey_ThrowsUnauthorized()
    {
        var (priv, _, fp) = ClientInteractor.GenerateRsaKeys();
        var (_, otherPub, _) = ClientInteractor.GenerateRsaKeys();
        var token = GateInteractor.IssueJwt("member-1", "client-1", priv, fp, "authorization", 1800);

        var ex = Assert.Throws<AppException>(() => GateInteractor.VerifyJwt("client-1", token, otherPub, "authorization"));

        Assert.Equal(401, ex.StatusCode);
    }

    [Fact]
    public void IssueJwt_CanBeCalledRepeatedly_WithoutDisposedObjectException()
    {
        var (priv, pub, fp) = ClientInteractor.GenerateRsaKeys();
        for (var i = 0; i < 5; i++)
        {
            var token  = GateInteractor.IssueJwt("member-1", "client-1", priv, fp, "authorization", 1800);
            var claims = GateInteractor.VerifyJwt("client-1", token, pub, "authorization");
            Assert.Equal("member-1", claims["sub"]);
        }
    }

    // ── IssueTokenAsync ──

    [Fact]
    public async Task IssueTokenAsync_ClientNotFound_ThrowsNotFound()
    {
        var uc = new GateInteractor(new FakeClientRepository(), new FakeGateCacheRepository(), Jwt);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.IssueTokenAsync(new GateIssueDto("unknown-token", "member-1")));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task IssueTokenAsync_CacheHit_ReturnsCachedTokenWithoutHistorySave()
    {
        var clientRepo  = new FakeClientRepository().Add(MakeClient());
        var cache       = new FakeGateCacheRepository();
        await cache.PutJwtAsync("client-1", "member-1", "cached-token", 1800);
        var historyRepo = new FakeJwtHistoryRepository();
        var uc = new GateInteractor(clientRepo, cache, Jwt, historyRepo);

        var vo = await uc.IssueTokenAsync(new GateIssueDto("token-1", "member-1"));

        Assert.Equal("cached-token", vo.Token);
        Assert.Empty(historyRepo.Saved);
    }

    [Fact]
    public async Task IssueTokenAsync_CacheMiss_IssuesNewTokenAndPersistsCacheAndHistory()
    {
        var clientRepo  = new FakeClientRepository().Add(MakeClient());
        var cache       = new FakeGateCacheRepository();
        var historyRepo = new FakeJwtHistoryRepository();
        var uc = new GateInteractor(clientRepo, cache, Jwt, historyRepo);

        var vo = await uc.IssueTokenAsync(new GateIssueDto("token-1", "member-1"));

        Assert.NotEmpty(vo.Token);
        Assert.Equal(1, cache.PutCallCount);
        Assert.Single(historyRepo.Saved);
        Assert.Equal("member-1", historyRepo.Saved[0].MemberId);
    }

    [Fact]
    public async Task IssueTokenAsync_CachePutFails_DoesNotThrow()
    {
        var clientRepo = new FakeClientRepository().Add(MakeClient());
        var cache      = new FakeGateCacheRepository { OnPut = () => throw new InvalidOperationException("redis down") };
        var uc = new GateInteractor(clientRepo, cache, Jwt);

        var vo = await uc.IssueTokenAsync(new GateIssueDto("token-1", "member-1"));

        Assert.NotEmpty(vo.Token);
    }

    [Fact]
    public async Task IssueTokenAsync_HistorySaveFails_DoesNotThrow()
    {
        var clientRepo  = new FakeClientRepository().Add(MakeClient());
        var cache       = new FakeGateCacheRepository();
        var historyRepo = new FakeJwtHistoryRepository { OnSave = () => throw new InvalidOperationException("db down") };
        var uc = new GateInteractor(clientRepo, cache, Jwt, historyRepo);

        var vo = await uc.IssueTokenAsync(new GateIssueDto("token-1", "member-1"));

        Assert.NotEmpty(vo.Token);
    }

    [Fact]
    public async Task IssueTokenAsync_NoHistoryRepoConfigured_DoesNotThrow()
    {
        var clientRepo = new FakeClientRepository().Add(MakeClient());
        var cache      = new FakeGateCacheRepository();
        var uc = new GateInteractor(clientRepo, cache, Jwt, historyRepo: null);

        var vo = await uc.IssueTokenAsync(new GateIssueDto("token-1", "member-1"));

        Assert.NotEmpty(vo.Token);
    }

    // ── VerifyAsync ──

    [Fact]
    public async Task VerifyAsync_ClientNotFound_ThrowsNotFound()
    {
        var uc = new GateInteractor(new FakeClientRepository(), new FakeGateCacheRepository(), Jwt);

        var ex = await Assert.ThrowsAsync<AppException>(() =>
            uc.VerifyAsync(new GateVerifyDto("unknown", "some-token")));

        Assert.Equal(404, ex.StatusCode);
    }

    [Fact]
    public async Task VerifyAsync_ValidToken_ReturnsClaims()
    {
        var client     = MakeClient();
        var clientRepo = new FakeClientRepository().Add(client);
        var uc = new GateInteractor(clientRepo, new FakeGateCacheRepository(), Jwt);
        var token = GateInteractor.IssueJwt("member-1", client.Identifier, client.PrivateKey, client.Fingerprint, Jwt.Issuer, Jwt.Ttl);

        var vo = await uc.VerifyAsync(new GateVerifyDto(client.Identifier, token));

        Assert.Equal("member-1", vo.Claims["sub"]);
    }
}
