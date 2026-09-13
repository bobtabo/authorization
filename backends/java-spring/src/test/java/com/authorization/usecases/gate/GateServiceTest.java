/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.gate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.authorization.config.AppConfig;
import com.authorization.domain.client.entities.Client;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.fakes.FakeClientRepository;
import com.authorization.support.fakes.FakeGateRepository;
import com.authorization.support.fakes.FakeJwtHistoryRepository;
import com.authorization.usecases.gate.dtos.GateIssueDto;
import com.authorization.usecases.gate.dtos.GateVerifyDto;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link GateService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class GateServiceTest {

    /**
     * アクセストークンに対応するクライアントが存在しない場合、401（client_not_found）を投げることを確認します。
     */
    @Test
    void issueTokenThrowsUnauthorizedWhenClientDoesNotExist() {
        GateService service = newService(new FakeClientRepository(), new FakeGateRepository(),
                new FakeJwtHistoryRepository(), jwtConfig());

        GateIssueDto dto = new GateIssueDto();
        dto.setAccessToken("unknown");
        dto.setMemberId("member-1");
        AppException exception = assertThrows(AppException.class, () -> service.issueToken(dto));

        assertEquals(401, exception.getStatusCode());
    }

    /**
     * キャッシュ済みJWTがある場合、それを返し新規発行しないことを確認します
     * （履歴保存・キャッシュ書き込みのいずれも呼ばれません）。
     */
    @Test
    void issueTokenReturnsCachedTokenWithoutIssuingNew() {
        RsaKeyPair keys = generateRsaKeyPair();
        Client client = makeClient(1L, "client-1", "token-1", keys);
        FakeClientRepository clientRepository = new FakeClientRepository().add(client);
        FakeGateRepository gateRepository = new FakeGateRepository().seed("client-1", "member-1", "cached-jwt");
        FakeJwtHistoryRepository historyRepository = new FakeJwtHistoryRepository();
        GateService service = newService(clientRepository, gateRepository, historyRepository, jwtConfig());

        GateIssueDto dto = new GateIssueDto();
        dto.setAccessToken("token-1");
        dto.setMemberId("member-1");
        var vo = service.issueToken(dto);

        assertEquals("cached-jwt", vo.getToken());
        assertEquals(0, historyRepository.getPersistCallCount());
        assertEquals(0, gateRepository.getPutJwtCallCount());
    }

    /**
     * 新規発行時は、JWT履歴を保存した後にキャッシュへ書き込むことを確認します
     * （履歴保存前にキャッシュを公開すると、保存失敗時に履歴の無いJWTがキャッシュに残るため）。
     */
    @Test
    void issueTokenSavesHistoryBeforeWritingCache() {
        RsaKeyPair keys = generateRsaKeyPair();
        Client client = makeClient(1L, "client-1", "token-1", keys);
        FakeClientRepository clientRepository = new FakeClientRepository().add(client);
        List<String> callLog = new ArrayList<>();
        FakeGateRepository gateRepository = new FakeGateRepository(callLog);
        FakeJwtHistoryRepository historyRepository = new FakeJwtHistoryRepository(callLog);
        GateService service = newService(clientRepository, gateRepository, historyRepository, jwtConfig());

        GateIssueDto dto = new GateIssueDto();
        dto.setAccessToken("token-1");
        dto.setMemberId("member-1");
        service.issueToken(dto);

        assertEquals(List.of("history", "cache"), callLog);
    }

    /**
     * 識別名に対応するクライアントが存在しない場合、403（client_not_found）を投げることを確認します。
     */
    @Test
    void verifyThrowsForbiddenWhenClientDoesNotExist() {
        GateService service = newService(new FakeClientRepository(), new FakeGateRepository(),
                new FakeJwtHistoryRepository(), jwtConfig());

        GateVerifyDto dto = new GateVerifyDto();
        dto.setIdentifier("unknown");
        dto.setToken("token");
        AppException exception = assertThrows(AppException.class, () -> service.verify(dto));

        assertEquals(403, exception.getStatusCode());
    }

    /**
     * 発行したJWTを同じクライアントで検証すると、クレームが正しく復元されることを確認します。
     */
    @Test
    void verifyReturnsClaimsForValidToken() {
        RsaKeyPair keys = generateRsaKeyPair();
        Client client = makeClient(1L, "client-1", "token-1", keys);
        FakeClientRepository clientRepository = new FakeClientRepository().add(client);
        GateService service = newService(
                clientRepository, new FakeGateRepository(), new FakeJwtHistoryRepository(), jwtConfig());

        GateIssueDto issueDto = new GateIssueDto();
        issueDto.setAccessToken("token-1");
        issueDto.setMemberId("member-1");
        String token = service.issueToken(issueDto).getToken();

        GateVerifyDto verifyDto = new GateVerifyDto();
        verifyDto.setIdentifier("client-1");
        verifyDto.setToken(token);
        var vo = service.verify(verifyDto);

        assertEquals("member-1", vo.getSub());
        assertEquals("client-1", vo.getAud());
        assertEquals("test-issuer", vo.getIss());
    }

    /**
     * 有効期限が切れたJWTを検証すると、401（jwt_invalid）を投げることを確認します。
     */
    @Test
    void verifyThrowsUnauthorizedForExpiredToken() {
        RsaKeyPair keys = generateRsaKeyPair();
        Client client = makeClient(1L, "client-1", "token-1", keys);
        FakeClientRepository clientRepository = new FakeClientRepository().add(client);
        AppConfig.Jwt expiredJwtConfig = new AppConfig.Jwt("test-issuer", "RS256", -10, 300);
        GateService service = newService(
                clientRepository, new FakeGateRepository(), new FakeJwtHistoryRepository(), expiredJwtConfig);

        GateIssueDto issueDto = new GateIssueDto();
        issueDto.setAccessToken("token-1");
        issueDto.setMemberId("member-1");
        String token = service.issueToken(issueDto).getToken();

        GateVerifyDto verifyDto = new GateVerifyDto();
        verifyDto.setIdentifier("client-1");
        verifyDto.setToken(token);
        AppException exception = assertThrows(AppException.class, () -> service.verify(verifyDto));

        assertEquals(401, exception.getStatusCode());
    }

    /**
     * 別のクライアントの公開鍵では署名を検証できず、401（jwt_invalid）を投げることを確認します。
     */
    @Test
    void verifyThrowsUnauthorizedWhenSignatureDoesNotMatch() {
        RsaKeyPair issuerKeys = generateRsaKeyPair();
        RsaKeyPair otherKeys = generateRsaKeyPair();
        FakeClientRepository clientRepository = new FakeClientRepository()
                .add(makeClient(1L, "client-1", "token-1", issuerKeys))
                .add(makeClient(2L, "client-2", "token-2", otherKeys));
        GateService service = newService(
                clientRepository, new FakeGateRepository(), new FakeJwtHistoryRepository(), jwtConfig());

        GateIssueDto issueDto = new GateIssueDto();
        issueDto.setAccessToken("token-1");
        issueDto.setMemberId("member-1");
        String token = service.issueToken(issueDto).getToken();

        GateVerifyDto verifyDto = new GateVerifyDto();
        verifyDto.setIdentifier("client-2");
        verifyDto.setToken(token);
        AppException exception = assertThrows(AppException.class, () -> service.verify(verifyDto));

        assertEquals(401, exception.getStatusCode());
    }

    /**
     * テスト用のGateServiceを組み立てます。
     *
     * @param clientRepository クライアントRepository（Fake）
     * @param gateRepository 認可Repository（Fake）
     * @param historyRepository JWT履歴Repository（Fake）
     * @param jwtConfig JWT設定
     * @return GateService
     */
    private static GateService newService(
            FakeClientRepository clientRepository,
            FakeGateRepository gateRepository,
            FakeJwtHistoryRepository historyRepository,
            AppConfig.Jwt jwtConfig) {
        return new GateService(clientRepository, gateRepository, historyRepository, jwtConfig);
    }

    /**
     * テスト用のJWT設定を組み立てます。
     *
     * @return JWT設定
     */
    private static AppConfig.Jwt jwtConfig() {
        return new AppConfig.Jwt("test-issuer", "RS256", 3600, 300);
    }

    /**
     * テスト用のクライアントEntityを組み立てます。
     *
     * @param id クライアントID
     * @param identifier クライアント識別名
     * @param accessToken アクセストークン
     * @param keys RSA鍵ペア
     * @return クライアントEntity
     */
    private static Client makeClient(long id, String identifier, String accessToken, RsaKeyPair keys) {
        Client client = new Client();
        client.setId(id);
        client.setIdentifier(identifier);
        client.setAccessToken(accessToken);
        client.setPrivateKey(keys.privatePem());
        client.setPublicKey(keys.publicPem());
        client.setFingerprint("SHA256:test-fingerprint-" + id);
        client.setStatus(com.authorization.domain.client.enums.ClientStatus.Active);
        client.setVersion(1);
        return client;
    }

    /**
     * テスト用のRSA鍵ペア（PEM形式）を生成します。実運用は4096bitですがテストでは高速化のため2048bitを使います。
     *
     * @return RSA鍵ペア
     */
    private static RsaKeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            String privatePem = "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                    + "\n-----END PRIVATE KEY-----\n";
            String publicPem = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            return new RsaKeyPair(privatePem, publicPem);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("failed to generate RSA key pair", e);
        }
    }

    /**
     * テスト用のRSA鍵ペア（PEM形式）です。
     *
     * @param privatePem 秘密鍵PEM
     * @param publicPem 公開鍵PEM
     */
    private record RsaKeyPair(String privatePem, String publicPem) {
    }
}
