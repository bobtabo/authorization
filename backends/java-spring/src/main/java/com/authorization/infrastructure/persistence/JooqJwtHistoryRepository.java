/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.JWT_HISTORIES;

import com.authorization.domain.client.condition.JwtHistoryCondition;
import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.domain.client.mappers.JwtHistoryRecordMapper;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import com.authorization.jooq.tables.records.JwtHistoriesRecord;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使ったJWT履歴Repositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqJwtHistoryRepository implements JwtHistoryRepository {

    private final DSLContext dsl;
    private final JwtHistoryRecordMapper recordMapper;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     * @param recordMapper JWT履歴 Entity/Record マッパー
     */
    public JooqJwtHistoryRepository(DSLContext dsl, JwtHistoryRecordMapper recordMapper) {
        this.dsl = dsl;
        this.recordMapper = recordMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByClientId(JwtHistoryCondition condition) {
        return dsl.fetchCount(dsl.selectFrom(JWT_HISTORIES).where(buildCondition(condition)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JwtHistory> findByClientId(JwtHistoryCondition condition) {
        var step = dsl.selectFrom(JWT_HISTORIES).where(buildCondition(condition));
        var option = condition.getOption();
        boolean asc = option != null && option.getOrderBy() != null;
        var sortField = option != null && "member_id".equals(
                option.getOrderBy() != null ? option.getOrderBy() : option.getOrderByDesc())
                ? JWT_HISTORIES.MEMBER_ID
                : JWT_HISTORIES.ISSUE_AT;
        step.orderBy(sortField.sort(asc ? SortOrder.ASC : SortOrder.DESC));
        if (option != null && condition.isPaging()) {
            step.limit(option.getOffset(), Math.clamp(option.getLimit(), 1, 500));
        }
        return step.fetch().map(recordMapper::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtHistory persist(JwtHistory entity) {
        JwtHistoriesRecord r = dsl.newRecord(JWT_HISTORIES);
        recordMapper.fillRecord(entity, r);
        r.store();
        entity.setId(r.getId());
        return entity;
    }

    /**
     * クライアントIDに紐づく未削除JWT履歴の絞り込み条件を組み立てます。
     *
     * @param condition 検索条件
     * @return 絞り込み条件
     */
    private static Condition buildCondition(JwtHistoryCondition condition) {
        return DSL.noCondition()
                .and(JWT_HISTORIES.CLIENT_ID.eq(condition.getClientId()))
                .and(JWT_HISTORIES.DELETED_AT.isNull());
    }
}
