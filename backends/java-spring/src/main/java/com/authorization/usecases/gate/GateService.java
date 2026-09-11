/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.gate;

import com.authorization.config.AppConfig;
import com.authorization.domain.client.condition.ClientCondition;
import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.domain.client.repositories.ClientRepository;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import com.authorization.domain.gate.repositories.GateRepository;
import com.authorization.domain.gate.valueobjects.GateIssueVo;
import com.authorization.domain.gate.valueobjects.GateVerifyVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.gate.dtos.GateIssueDto;
import com.authorization.usecases.gate.dtos.GateVerifyDto;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * 認可Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class GateService extends AbstractService {

    private final ClientRepository clientRepository;
    private final GateRepository gateRepository;
    private final JwtHistoryRepository historyRepository;
    private final AppConfig.Jwt jwtConfig;

    /**
     * コンストラクタ。
     *
     * @param clientRepository クライアントRepository
     * @param gateRepository 認可Repository
     * @param historyRepository JWT履歴Repository
     * @param jwtConfig JWT設定
     */
    public GateService(
            ClientRepository clientRepository,
            GateRepository gateRepository,
            JwtHistoryRepository historyRepository,
            AppConfig.Jwt jwtConfig) {
        this.clientRepository = clientRepository;
        this.gateRepository = gateRepository;
        this.historyRepository = historyRepository;
        this.jwtConfig = jwtConfig;
    }

    /**
     * JWTを発行します。
     *
     * @param dto JWT発行DTO
     * @return JWT発行結果ValueObject
     */
    public GateIssueVo issueToken(GateIssueDto dto) {
        ClientCondition condition = new ClientCondition();
        condition.setAccessToken(dto.getAccessToken());
        Client client = clientRepository.findByAccessToken(condition);
        if (client == null) {
            throw AppException.unauthorized("client_not_found");
        }

        String identifier = client.getIdentifier();
        String token = gateRepository.getJwt(identifier, dto.getMemberId());

        if (token == null) {
            token = issueJwt(dto.getMemberId(), identifier, client.getPrivateKey(), client.getFingerprint());
            gateRepository.putJwt(identifier, dto.getMemberId(), token, (int) jwtConfig.cacheTtl());

            JwtHistory history = new JwtHistory();
            history.setClientId(client.getId());
            history.setMemberId(dto.getMemberId());
            history.setIssueAt(LocalDateTime.now());
            history.setJwt(token);
            history.assignCreated(0);
            historyRepository.persist(history);
        }

        GateIssueVo vo = new GateIssueVo();
        vo.setToken(token);
        return vo;
    }

    /**
     * JWTを検証します。
     *
     * @param dto JWT検証リクエスト用DTO
     * @return JWT検証結果ValueObject
     */
    public GateVerifyVo verify(GateVerifyDto dto) {
        ClientCondition condition = new ClientCondition();
        condition.setIdentifier(dto.getIdentifier());
        Client client = clientRepository.findByIdentifier(condition);
        if (client == null) {
            throw AppException.forbidden("client_not_found");
        }

        return verifyJwt(dto.getIdentifier(), dto.getToken(), client.getPublicKey());
    }

    /**
     * RS256 で署名した JWT を発行します。
     *
     * @param memberId クライアント会員ID（sub）
     * @param identifier クライアント識別名（aud）
     * @param privateKeyPem 署名用RSA秘密鍵（PEM形式）
     * @param fingerprint 秘密鍵フィンガープリント（kid）
     * @return 発行したJWT文字列
     */
    private String issueJwt(String memberId, String identifier, String privateKeyPem, String fingerprint) {
        try {
            RSAPrivateKey privateKey = loadPrivateKey(privateKeyPem);
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(jwtConfig.issuer())
                    .subject(memberId)
                    .audience(identifier)
                    .expirationTime(Date.from(now.plusSeconds(jwtConfig.ttl())))
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now))
                    .jwtID(UUID.randomUUID().toString())
                    .build();
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(fingerprint).build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (JOSEException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("failed to issue jwt", e);
        }
    }

    /**
     * JWT を検証しデコードした Payload を ValueObject で返します。
     *
     * @param identifier クライアント識別名（aud 検証に使用）
     * @param token JWT 文字列
     * @param publicKeyPem 検証用RSA公開鍵（PEM形式）
     * @return JWT検証結果ValueObject
     */
    private GateVerifyVo verifyJwt(String identifier, String token, String publicKeyPem) {
        JWTClaimsSet claims;
        try {
            RSAPublicKey publicKey = loadPublicKey(publicKeyPem);
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new RSASSAVerifier(publicKey))) {
                throw AppException.unauthorized("jwt_invalid");
            }
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException | JOSEException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw AppException.unauthorized("jwt_invalid");
        }

        if (claims.getExpirationTime() != null && new Date().after(claims.getExpirationTime())) {
            throw AppException.unauthorized("jwt_invalid");
        }
        if (!jwtConfig.issuer().equals(claims.getIssuer())) {
            throw AppException.unauthorized("jwt_invalid");
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(identifier)) {
            throw AppException.forbidden("jwt_invalid");
        }

        GateVerifyVo vo = new GateVerifyVo();
        vo.setIss(claims.getIssuer());
        vo.setSub(claims.getSubject());
        vo.setAud(identifier);
        vo.setExp(claims.getExpirationTime() != null ? claims.getExpirationTime().getTime() / 1000 : 0);
        vo.setIat(claims.getIssueTime() != null ? claims.getIssueTime().getTime() / 1000 : 0);
        vo.setNbf(claims.getNotBeforeTime() != null ? claims.getNotBeforeTime().getTime() / 1000 : 0);
        vo.setJti(claims.getJWTID());
        return vo;
    }

    private static RSAPrivateKey loadPrivateKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String cleaned = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(cleaned);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private static RSAPublicKey loadPublicKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String cleaned = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(cleaned);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }
}
