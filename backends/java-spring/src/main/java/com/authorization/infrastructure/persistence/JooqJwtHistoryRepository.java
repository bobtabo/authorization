/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.JWT_HISTORIES;

import com.authorization.domain.client.condition.JwtHistoryCondition;
import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.SortOrder;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使ったJWT履歴Repositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqJwtHistoryRepository implements JwtHistoryRepository {

    private final DSLContext dsl;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     */
    public JooqJwtHistoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByClientId(JwtHistoryCondition condition) {
        return dsl.fetchCount(baseQuery(condition));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JwtHistory> findByClientId(JwtHistoryCondition condition) {
        SelectQuery<Record> q = baseQuery(condition);
        var option = condition.getOption();
        boolean asc = option != null && option.getOrderBy() != null;
        var sortField = option != null && "member_id".equals(
                option.getOrderBy() != null ? option.getOrderBy() : option.getOrderByDesc())
                ? JWT_HISTORIES.MEMBER_ID
                : JWT_HISTORIES.ISSUE_AT;
        q.addOrderBy(sortField.sort(asc ? SortOrder.ASC : SortOrder.DESC));
        if (option != null && condition.isPaging()) {
            q.addLimit(option.getOffset(), Math.clamp(option.getLimit(), 1, 500));
        }
        return q.fetch().map(JooqJwtHistoryRepository::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtHistory persist(JwtHistory entity) {
        LocalDateTime now = LocalDateTime.now();
        var r = dsl.newRecord(JWT_HISTORIES);
        r.setClientId(entity.getClientId());
        r.setMemberId(entity.getMemberId());
        r.setIssueAt(entity.getIssueAt());
        r.setJwt(entity.getJwt());
        r.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : now);
        r.setCreatedBy(entity.getCreatedBy());
        r.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : now);
        r.setUpdatedBy(entity.getUpdatedBy());
        r.setVersion((long) (entity.getVersion() != null ? entity.getVersion() : 1));
        r.store();
        entity.setId(r.getId());
        return entity;
    }

    /**
     * クライアントIDに紐づく未削除JWT履歴クエリを組み立てます。
     *
     * @param condition 検索条件
     * @return クエリ
     */
    private SelectQuery<Record> baseQuery(JwtHistoryCondition condition) {
        SelectQuery<Record> q = dsl.selectQuery();
        q.addFrom(JWT_HISTORIES);
        q.addConditions(JWT_HISTORIES.CLIENT_ID.eq(condition.getClientId()));
        q.addConditions(JWT_HISTORIES.DELETED_AT.isNull());
        return q;
    }

    /**
     * jOOQレコードをJWT履歴エンティティへ変換します。
     *
     * @param rec jOOQレコード
     * @return JWT履歴エンティティ
     */
    private static JwtHistory toEntity(Record rec) {
        JwtHistory h = new JwtHistory();
        h.setId(rec.get(JWT_HISTORIES.ID));
        h.setClientId(rec.get(JWT_HISTORIES.CLIENT_ID));
        h.setMemberId(rec.get(JWT_HISTORIES.MEMBER_ID));
        h.setIssueAt(rec.get(JWT_HISTORIES.ISSUE_AT));
        h.setJwt(rec.get(JWT_HISTORIES.JWT));
        h.setCreatedAt(rec.get(JWT_HISTORIES.CREATED_AT));
        h.setDeletedAt(rec.get(JWT_HISTORIES.DELETED_AT));
        return h;
    }
}
