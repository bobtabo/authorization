/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.client;

import com.authorization.domain.client.condition.ClientCondition;
import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.domain.client.mappers.ClientApiMapper;
import com.authorization.domain.client.mappers.ClientConditionMapper;
import com.authorization.domain.client.mappers.ClientDtoMapper;
import com.authorization.domain.client.repositories.ClientRepository;
import com.authorization.domain.client.valueobjects.ClientDetailVo;
import com.authorization.domain.client.valueobjects.ClientInfoVo;
import com.authorization.domain.client.valueobjects.ClientListVo;
import com.authorization.domain.client.valueobjects.ClientQrVo;
import com.authorization.domain.client.valueobjects.ClientStartVo;
import com.authorization.domain.client.valueobjects.ClientStoreVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.repositories.conditions.Option;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.client.dtos.ClientDto;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * クライアントServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class ClientService extends AbstractService {

    private static final DateTimeFormatter FMT_FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClientRepository repository;
    private final ClientConditionMapper conditionMapper;
    private final ClientApiMapper apiMapper;
    private final ClientDtoMapper dtoMapper;

    /**
     * コンストラクタ。
     *
     * @param repository クライアントRepository
     * @param conditionMapper DTO→Condition マッパー
     * @param apiMapper Entity→ValueObject マッパー
     * @param dtoMapper DTO→Entity マッパー
     */
    public ClientService(
            ClientRepository repository,
            ClientConditionMapper conditionMapper,
            ClientApiMapper apiMapper,
            ClientDtoMapper dtoMapper) {
        this.repository = repository;
        this.conditionMapper = conditionMapper;
        this.apiMapper = apiMapper;
        this.dtoMapper = dtoMapper;
    }

    /**
     * アクセストークンでクライアントを認証します。
     *
     * @param dto クライアントDTO
     * @return 認証成功の場合 true
     */
    public boolean authenticateByToken(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        return repository.findByAccessToken(condition) != null;
    }

    /**
     * クライアント一覧を取得します。
     *
     * @param dto クライアントDTO
     * @return クライアント一覧ValueObject
     */
    public ClientListVo getClients(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        condition.setStartFrom(parseDate(dto.getStartFrom()));
        condition.setStartTo(parseDate(dto.getStartTo()));
        condition.setOption(new Option(dto.getOffset(), dto.getLimit(), dto.getSort(), dto.getSortType()));

        int count = repository.countByCondition(condition);
        var list = repository.findByCondition(condition);

        ClientListVo vo = new ClientListVo();
        vo.setClients(list);
        vo.setCount(count);
        vo.setOffset(dto.getOffset());
        vo.setLimit(dto.getLimit());
        vo.setSort(dto.getSort());
        vo.setSortType(dto.getSortType());
        return vo;
    }

    /**
     * クライアント詳細を取得します。
     *
     * @param dto クライアントDTO
     * @return クライアント詳細ValueObject
     */
    public ClientDetailVo show(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findById(condition);

        if (entity == null) {
            return new ClientDetailVo();
        }
        return apiMapper.toDetailVo(entity);
    }

    /**
     * クライアントを登録します。
     *
     * @param dto クライアントDTO
     * @return クライアント登録ValueObject
     */
    public ClientStoreVo store(ClientDto dto) {
        RsaKeyPair keys = generateRsaKeys();

        Client entity = new Client();
        dtoMapper.applyBasicInfo(dto, entity);
        entity.setIdentifier(generateHex(8));
        entity.setStatus(ClientStatus.Inactive);
        entity.setPrivateKey(keys.privatePem());
        entity.setPublicKey(keys.publicPem());
        entity.setFingerprint(keys.fingerprint());
        entity.setAccessToken(generateHex(32));
        entity.assignCreated(dto.getExecutorId() == null ? 0 : dto.getExecutorId());

        Client saved = repository.persist(entity);

        return apiMapper.toStoreVo(saved);
    }

    /**
     * クライアントを更新します。
     *
     * @param dto クライアントDTO
     * @return クライアント更新ValueObject
     */
    public ClientStoreVo update(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findById(condition);
        if (entity == null) {
            throw AppException.notFound("client_not_found");
        }
        if (dto.getVersion() != null && !dto.getVersion().equals(entity.getVersion())) {
            throw new AppException(409, "optimistic_lock");
        }

        // identifier は登録時に自動生成するため更新不可。
        // status は下記の遷移ロジックで個別に制御する。accessToken は不変。
        // dtoMapper.applyBasicInfo はDTOの値がnullのプロパティを上書きしないため、
        // 部分更新（PATCH）としてそのまま使える。
        dtoMapper.applyBasicInfo(dto, entity);

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
            if (dto.getStatus() == ClientStatus.Active && entity.getStartAt() == null) {
                entity.setStartAt(LocalDateTime.now());
                entity.setStopAt(null);
            }
            if (dto.getStatus() == ClientStatus.Suspended) {
                entity.setStopAt(LocalDateTime.now());
            }
        }

        entity.assignUpdated(dto.getExecutorId() == null ? 0 : dto.getExecutorId());
        Client saved = repository.persist(entity);

        return apiMapper.toStoreVo(saved);
    }

    /**
     * クライアントを論理削除します。削除前にステータスを Closed に更新します。
     *
     * @param dto クライアントDTO
     */
    public void destroy(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findById(condition);
        if (entity == null) {
            throw AppException.notFound("client_not_found");
        }
        if (dto.getVersion() != null && !dto.getVersion().equals(entity.getVersion())) {
            throw new AppException(409, "optimistic_lock");
        }

        entity.setStatus(ClientStatus.Closed);
        entity.assignUpdated(dto.getExecutorId() == null ? 0 : dto.getExecutorId());
        Client saved = repository.persist(entity);

        saved.assignDeleted(dto.getExecutorId() == null ? 0 : dto.getExecutorId());
        repository.deleteById(saved);
    }

    /**
     * スマホ連携用QRコードデータを返します。
     *
     * @param dto クライアントDTO
     * @return QRコード用ValueObject
     */
    public ClientQrVo getQr(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findByIdentifier(condition);
        if (entity == null) {
            throw AppException.notFound("client_not_found");
        }

        ClientQrVo vo = new ClientQrVo();
        vo.setIdentifier(entity.getIdentifier());
        vo.setDeeplinkUrl("authgateway://clients/" + entity.getIdentifier() + "/info");
        return vo;
    }

    /**
     * スマホアプリからの利用開始処理を行い、アクセストークンを返します。
     * Inactive / Suspended の場合は Active に遷移します。既に Active の場合もトークンを返します。
     *
     * @param dto クライアントDTO
     * @return 利用開始ValueObject
     */
    public ClientStartVo start(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findByIdentifier(condition);
        if (entity == null) {
            throw AppException.notFound("client_not_found");
        }

        if (entity.getStatus() != ClientStatus.Active) {
            entity.setStatus(ClientStatus.Active);
            if (entity.getStartAt() == null) {
                entity.setStartAt(LocalDateTime.now());
            }
            entity.setStopAt(null);
            entity.assignUpdated(0);
            entity = repository.persist(entity);
        }

        ClientStartVo vo = new ClientStartVo();
        vo.setAccessToken(entity.getAccessToken());
        return vo;
    }

    /**
     * スマホアプリからの利用停止処理を行います。
     * Active の場合は Suspended に遷移します。既に Suspended / Inactive / Closed の場合は何もしません。
     *
     * @param dto クライアントDTO
     */
    public void stop(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findByIdentifier(condition);
        if (entity == null) {
            throw AppException.notFound("client_not_found");
        }

        if (entity.getStatus() == ClientStatus.Active) {
            entity.setStatus(ClientStatus.Suspended);
            entity.setStopAt(LocalDateTime.now());
            entity.assignUpdated(0);
            repository.persist(entity);
        }
    }

    /**
     * スマホアプリ向けにクライアント情報を返します。アクセストークンで認証済みのクライアントを対象とします。
     *
     * @param dto クライアントDTO
     * @return クライアント情報ValueObject
     */
    public ClientInfoVo getInfo(ClientDto dto) {
        ClientCondition condition = conditionMapper.toCondition(dto);
        Client entity = repository.findByIdentifier(condition);
        if (entity == null) {
            throw AppException.notFound("client_not_found");
        }

        ClientInfoVo vo = new ClientInfoVo();
        vo.setIdentifier(entity.getIdentifier());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus().value());
        return vo;
    }

    /**
     * "yyyy-MM-dd HH:mm:ss" または "yyyy-MM-dd" 形式の日付文字列を解析します。
     *
     * @param s 日付文字列
     * @return 解析結果。解析できない場合は null
     */
    private static LocalDateTime parseDate(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, FMT_FULL);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(s, FMT_DATE).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    /**
     * 暗号論的乱数から 16 進文字列を生成します。
     *
     * @param byteCount 生成するランダムバイト数
     * @return 小文字16進文字列
     */
    private static String generateHex(int byteCount) {
        byte[] buf = new byte[byteCount];
        new SecureRandom().nextBytes(buf);
        StringBuilder sb = new StringBuilder(byteCount * 2);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * RSA 4096bit 鍵ペアを生成し、秘密鍵PEM・公開鍵PEM・SHA256フィンガープリントを返します。
     *
     * @return 鍵ペア
     */
    private static RsaKeyPair generateRsaKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            var kp = kpg.generateKeyPair();

            String privPem = "-----BEGIN PRIVATE KEY-----\n"
                    + wrap(Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()))
                    + "\n-----END PRIVATE KEY-----\n";
            String pubPem = "-----BEGIN PUBLIC KEY-----\n"
                    + wrap(Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()))
                    + "\n-----END PUBLIC KEY-----\n";

            byte[] hash = MessageDigest.getInstance("SHA-256").digest(kp.getPublic().getEncoded());
            String fingerprint = "SHA256:" + Base64.getEncoder().withoutPadding().encodeToString(hash);

            return new RsaKeyPair(privPem, pubPem, fingerprint);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("failed to generate RSA key pair", e);
        }
    }

    /**
     * Base64 文字列を 64 文字ごとに改行して PEM 形式に整形します。
     *
     * @param base64 Base64 文字列
     * @return 改行整形済み文字列
     */
    private static String wrap(String base64) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base64.length(); i += 64) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(base64, i, Math.min(i + 64, base64.length()));
        }
        return sb.toString();
    }

    private record RsaKeyPair(String privatePem, String publicPem, String fingerprint) {
    }
}
