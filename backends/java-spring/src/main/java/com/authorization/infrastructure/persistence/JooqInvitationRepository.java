/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.INVITATIONS;

import com.authorization.domain.invitation.condition.InvitationCondition;
import com.authorization.domain.invitation.entities.Invitation;
import com.authorization.domain.invitation.mappers.InvitationRecordMapper;
import com.authorization.domain.invitation.repositories.InvitationRepository;
import com.authorization.jooq.tables.records.InvitationsRecord;
import com.authorization.support.exceptions.AppException;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使った招待Repositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqInvitationRepository implements InvitationRepository {

    private final DSLContext dsl;
    private final InvitationRecordMapper recordMapper;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     * @param recordMapper 招待 Entity/Record マッパー
     */
    public JooqInvitationRepository(DSLContext dsl, InvitationRecordMapper recordMapper) {
        this.dsl = dsl;
        this.recordMapper = recordMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Invitation getCurrentByRole(int role) {
        InvitationsRecord rec = dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.ROLE.eq((long) role))
                .and(INVITATIONS.DELETED_AT.isNull())
                .orderBy(INVITATIONS.CREATED_AT.desc())
                .limit(1)
                .fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Invitation persist(Invitation entity) {
        int rows = dsl.update(INVITATIONS)
                .set(INVITATIONS.TOKEN, entity.getToken())
                .set(INVITATIONS.UPDATED_AT, entity.getUpdatedAt())
                .set(INVITATIONS.UPDATED_BY, entity.getUpdatedBy())
                .set(INVITATIONS.VERSION, (long) (entity.getVersion() + 1))
                .where(INVITATIONS.ID.eq(entity.getId()))
                .and(INVITATIONS.VERSION.eq((long) entity.getVersion()))
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
    public Invitation findByToken(InvitationCondition condition) {
        InvitationsRecord rec = dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.TOKEN.eq(condition.getToken()))
                .and(INVITATIONS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }
}
