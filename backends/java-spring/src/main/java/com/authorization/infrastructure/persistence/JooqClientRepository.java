/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.CLIENTS;

import com.authorization.domain.client.condition.ClientCondition;
import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.domain.client.repositories.ClientRepository;
import com.authorization.jooq.tables.records.ClientsRecord;
import com.authorization.support.exceptions.AppException;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.SortOrder;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使ったクライアントRepositoryの実装です。永続化技術に合わせて
 * {@code Jooq<Domain>Repository} という命名にしています（PHP版はEloquentのため
 * {@code Eloquent<Domain>Repository}）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqClientRepository implements ClientRepository {

    private final DSLContext dsl;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     */
    public JooqClientRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Client> findByCondition(ClientCondition condition) {
        SelectQuery<Record> q = applyFilters(condition);
        if (condition.getOption() != null) {
            q.addOrderBy(sortField(condition.getOption().getOrderBy(), condition.getOption().getOrderByDesc())
                    .sort(condition.getOption().getOrderByDesc() != null ? SortOrder.DESC : SortOrder.ASC));
            if (condition.isPaging()) {
                q.addLimit(condition.getOption().getOffset(), Math.clamp(condition.getOption().getLimit(), 1, 500));
            }
        }
        return q.fetch().map(JooqClientRepository::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByCondition(ClientCondition condition) {
        return dsl.fetchCount(applyFilters(condition));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client findById(ClientCondition condition) {
        Record rec = dsl.selectFrom(CLIENTS).where(CLIENTS.ID.eq(condition.getId())).fetchOne();
        return rec == null ? null : toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client findByAccessToken(ClientCondition condition) {
        Record rec = dsl.selectFrom(CLIENTS)
                .where(CLIENTS.ACCESS_TOKEN.eq(condition.getAccessToken()))
                .and(CLIENTS.STATUS.eq((long) ClientStatus.Active.value()))
                .and(CLIENTS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client findByIdentifier(ClientCondition condition) {
        Record rec = dsl.selectFrom(CLIENTS)
                .where(CLIENTS.IDENTIFIER.eq(condition.getIdentifier()))
                .and(CLIENTS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client persist(Client entity) {
        if (entity.getId() == null) {
            ClientsRecord r = dsl.newRecord(CLIENTS);
            fillRecord(r, entity);
            r.store();
            entity.setId(r.getId());
            return entity;
        }

        int rows = dsl.update(CLIENTS)
                .set(CLIENTS.NAME, entity.getName())
                .set(CLIENTS.POST_CODE, entity.getPostCode())
                .set(CLIENTS.PREF, entity.getPref())
                .set(CLIENTS.CITY, entity.getCity())
                .set(CLIENTS.ADDRESS, entity.getAddress())
                .set(CLIENTS.BUILDING, entity.getBuilding())
                .set(CLIENTS.TEL, entity.getTel())
                .set(CLIENTS.EMAIL, entity.getEmail())
                .set(CLIENTS.STATUS, (long) entity.getStatus().value())
                .set(CLIENTS.START_AT, entity.getStartAt())
                .set(CLIENTS.STOP_AT, entity.getStopAt())
                .set(CLIENTS.UPDATED_AT, entity.getUpdatedAt())
                .set(CLIENTS.UPDATED_BY, entity.getUpdatedBy())
                .set(CLIENTS.VERSION, (long) (entity.getVersion() + 1))
                .where(CLIENTS.ID.eq(entity.getId()))
                .and(CLIENTS.VERSION.eq((long) entity.getVersion()))
                .execute();
        if (rows == 0) {
            throw new AppException(409, "optimistic_lock");
        }
        entity.setVersion(entity.getVersion() + 1);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteById(Client entity) {
        int rows = dsl.update(CLIENTS)
                .set(CLIENTS.DELETED_AT, entity.getDeletedAt())
                .set(CLIENTS.DELETED_BY, entity.getDeletedBy())
                .where(CLIENTS.ID.eq(entity.getId()))
                .and(CLIENTS.VERSION.eq((long) entity.getVersion()))
                .execute();
        return rows > 0;
    }

    /**
     * 検索条件から絞り込みクエリを組み立てます（ページング・並び順は含まない）。
     *
     * @param condition 検索条件
     * @return 絞り込み済みクエリ
     */
    private SelectQuery<Record> applyFilters(ClientCondition condition) {
        SelectQuery<Record> q = dsl.selectQuery();
        q.addFrom(CLIENTS);
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            String kw = "%" + condition.getKeyword() + "%";
            q.addConditions(CLIENTS.NAME.like(kw).or(CLIENTS.EMAIL.like(kw)));
        }
        if (condition.getStartFrom() != null) {
            q.addConditions(CLIENTS.START_AT.greaterOrEqual(condition.getStartFrom()));
        }
        if (condition.getStartTo() != null) {
            q.addConditions(CLIENTS.START_AT.lessOrEqual(condition.getStartTo()));
        }
        if (!condition.getStatuses().isEmpty()) {
            q.addConditions(CLIENTS.STATUS.in(condition.getStatuses().stream().map(Long::valueOf).toList()));
        }
        return q;
    }

    /**
     * ソート対象のフィールドを解決します。
     *
     * @param orderBy 昇順ソート対象カラム名
     * @param orderByDesc 降順ソート対象カラム名
     * @return jOOQ フィールド
     */
    private static org.jooq.Field<?> sortField(String orderBy, String orderByDesc) {
        String column = orderBy != null ? orderBy : orderByDesc;
        return switch (column == null ? "" : column) {
            case "name" -> CLIENTS.NAME;
            case "status" -> CLIENTS.STATUS;
            case "updated_at" -> CLIENTS.UPDATED_AT;
            case "start_at" -> CLIENTS.START_AT;
            default -> CLIENTS.CREATED_AT;
        };
    }

    /**
     * エンティティの値をjOOQレコードへ設定します（新規登録用）。
     *
     * @param r jOOQレコード
     * @param entity クライアントエンティティ
     */
    private static void fillRecord(ClientsRecord r, Client entity) {
        r.setName(entity.getName());
        r.setIdentifier(entity.getIdentifier());
        r.setPostCode(entity.getPostCode());
        r.setPref(entity.getPref());
        r.setCity(entity.getCity());
        r.setAddress(entity.getAddress());
        r.setBuilding(entity.getBuilding());
        r.setTel(entity.getTel());
        r.setEmail(entity.getEmail());
        r.setAccessToken(entity.getAccessToken());
        r.setPrivateKey(entity.getPrivateKey());
        r.setPublicKey(entity.getPublicKey());
        r.setFingerprint(entity.getFingerprint());
        r.setStatus((long) entity.getStatus().value());
        r.setStartAt(entity.getStartAt());
        r.setStopAt(entity.getStopAt());
        r.setCreatedAt(entity.getCreatedAt());
        r.setCreatedBy(entity.getCreatedBy());
        r.setUpdatedAt(entity.getUpdatedAt());
        r.setUpdatedBy(entity.getUpdatedBy());
        r.setVersion((long) entity.getVersion());
    }

    /**
     * jOOQレコードをクライアントエンティティへ変換します。
     *
     * @param rec jOOQレコード
     * @return クライアントエンティティ
     */
    private static Client toEntity(Record rec) {
        Client c = new Client();
        c.setId(rec.get(CLIENTS.ID));
        c.setName(rec.get(CLIENTS.NAME));
        c.setIdentifier(rec.get(CLIENTS.IDENTIFIER));
        c.setPostCode(rec.get(CLIENTS.POST_CODE));
        c.setPref(rec.get(CLIENTS.PREF));
        c.setCity(rec.get(CLIENTS.CITY));
        c.setAddress(rec.get(CLIENTS.ADDRESS));
        c.setBuilding(rec.get(CLIENTS.BUILDING) != null ? rec.get(CLIENTS.BUILDING) : "");
        c.setTel(rec.get(CLIENTS.TEL));
        c.setEmail(rec.get(CLIENTS.EMAIL));
        c.setAccessToken(rec.get(CLIENTS.ACCESS_TOKEN));
        c.setPrivateKey(rec.get(CLIENTS.PRIVATE_KEY));
        c.setPublicKey(rec.get(CLIENTS.PUBLIC_KEY));
        c.setFingerprint(rec.get(CLIENTS.FINGERPRINT));
        c.setStatus(ClientStatus.from(rec.get(CLIENTS.STATUS).intValue()));
        c.setStartAt(rec.get(CLIENTS.START_AT));
        c.setStopAt(rec.get(CLIENTS.STOP_AT));
        c.setCreatedAt(rec.get(CLIENTS.CREATED_AT));
        c.setCreatedBy(rec.get(CLIENTS.CREATED_BY));
        c.setUpdatedAt(rec.get(CLIENTS.UPDATED_AT));
        c.setUpdatedBy(rec.get(CLIENTS.UPDATED_BY));
        c.setDeletedAt(rec.get(CLIENTS.DELETED_AT));
        c.setDeletedBy(rec.get(CLIENTS.DELETED_BY));
        c.setVersion(rec.get(CLIENTS.VERSION).intValue());
        return c;
    }
}
