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
import com.authorization.domain.client.mappers.ClientRecordMapper;
import com.authorization.domain.client.repositories.ClientRepository;
import com.authorization.jooq.tables.records.ClientsRecord;
import com.authorization.support.exceptions.AppException;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
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
    private final ClientRecordMapper recordMapper;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     * @param recordMapper クライアント Entity/Record マッパー
     */
    public JooqClientRepository(DSLContext dsl, ClientRecordMapper recordMapper) {
        this.dsl = dsl;
        this.recordMapper = recordMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Client> findByCondition(ClientCondition condition) {
        var step = dsl.selectFrom(CLIENTS).where(buildCondition(condition));
        if (condition.getOption() != null) {
            step.orderBy(sortField(condition.getOption().getOrderBy(), condition.getOption().getOrderByDesc())
                    .sort(condition.getOption().getOrderByDesc() != null ? SortOrder.DESC : SortOrder.ASC));
            if (condition.isPaging()) {
                step.limit(condition.getOption().getOffset(), Math.clamp(condition.getOption().getLimit(), 1, 500));
            }
        }
        return step.fetch().map(recordMapper::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByCondition(ClientCondition condition) {
        return dsl.fetchCount(dsl.selectFrom(CLIENTS).where(buildCondition(condition)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client findById(ClientCondition condition) {
        ClientsRecord rec = dsl.selectFrom(CLIENTS).where(CLIENTS.ID.eq(condition.getId())).fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client findByAccessToken(ClientCondition condition) {
        ClientsRecord rec = dsl.selectFrom(CLIENTS)
                .where(CLIENTS.ACCESS_TOKEN.eq(condition.getAccessToken()))
                .and(CLIENTS.STATUS.eq((long) ClientStatus.Active.value()))
                .and(CLIENTS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client findByIdentifier(ClientCondition condition) {
        ClientsRecord rec = dsl.selectFrom(CLIENTS)
                .where(CLIENTS.IDENTIFIER.eq(condition.getIdentifier()))
                .and(CLIENTS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client persist(Client entity) {
        if (entity.getId() == null) {
            ClientsRecord r = dsl.newRecord(CLIENTS);
            recordMapper.fillRecord(entity, r);
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
     * 検索条件から絞り込み条件を組み立てます（ページング・並び順は含まない）。
     *
     * @param condition 検索条件
     * @return 絞り込み条件
     */
    private static Condition buildCondition(ClientCondition condition) {
        Condition cond = DSL.noCondition();
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            String kw = "%" + condition.getKeyword() + "%";
            cond = cond.and(CLIENTS.NAME.like(kw).or(CLIENTS.EMAIL.like(kw)));
        }
        if (condition.getStartFrom() != null) {
            cond = cond.and(CLIENTS.START_AT.greaterOrEqual(condition.getStartFrom()));
        }
        if (condition.getStartTo() != null) {
            cond = cond.and(CLIENTS.START_AT.lessOrEqual(condition.getStartTo()));
        }
        if (!condition.getStatuses().isEmpty()) {
            cond = cond.and(CLIENTS.STATUS.in(condition.getStatuses().stream().map(Long::valueOf).toList()));
        }
        return cond;
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
}
