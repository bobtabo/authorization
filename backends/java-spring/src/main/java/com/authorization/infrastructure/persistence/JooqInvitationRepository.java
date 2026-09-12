/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.INVITATIONS;

import com.authorization.domain.invitation.condition.InvitationCondition;
import com.authorization.domain.invitation.entities.Invitation;
import com.authorization.domain.invitation.repositories.InvitationRepository;
import com.authorization.support.exceptions.AppException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使った招待Repositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqInvitationRepository implements InvitationRepository {

    private final DSLContext dsl;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     */
    public JooqInvitationRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Invitation getCurrentByRole(int role) {
        Record rec = dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.ROLE.eq((long) role))
                .and(INVITATIONS.DELETED_AT.isNull())
                .orderBy(INVITATIONS.CREATED_AT.desc())
                .limit(1)
                .fetchOne();
        return rec == null ? null : toEntity(rec);
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
        Record rec = dsl.selectFrom(INVITATIONS)
                .where(INVITATIONS.TOKEN.eq(condition.getToken()))
                .and(INVITATIONS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : toEntity(rec);
    }

    /**
     * jOOQレコードを招待エンティティへ変換します。
     *
     * @param rec jOOQレコード
     * @return 招待エンティティ
     */
    private static Invitation toEntity(Record rec) {
        Invitation i = new Invitation();
        i.setId(rec.get(INVITATIONS.ID));
        i.setToken(rec.get(INVITATIONS.TOKEN));
        i.setRole(rec.get(INVITATIONS.ROLE).intValue());
        i.setCreatedAt(rec.get(INVITATIONS.CREATED_AT));
        i.setUpdatedAt(rec.get(INVITATIONS.UPDATED_AT));
        i.setVersion(rec.get(INVITATIONS.VERSION).intValue());
        return i;
    }
}
