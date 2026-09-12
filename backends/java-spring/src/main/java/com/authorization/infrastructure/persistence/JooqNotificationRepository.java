/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.NOTIFICATIONS;

import com.authorization.domain.notification.condition.NotificationCondition;
import com.authorization.domain.notification.entities.Notification;
import com.authorization.domain.notification.repositories.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使った通知Repositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqNotificationRepository implements NotificationRepository {

    private final DSLContext dsl;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     */
    public JooqNotificationRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> listPage(NotificationCondition condition) {
        Long afterId = decodeCursor(condition.getCursor());
        var q = dsl.selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()))
                .and(NOTIFICATIONS.DELETED_AT.isNull());
        if (afterId != null) {
            q.and(NOTIFICATIONS.ID.lessThan(afterId));
        }
        // カーソルページングでは +1 件多く取得し、次ページの有無を判定する（Service側の責務）。
        return q.orderBy(NOTIFICATIONS.ID.desc())
                .limit(condition.getLimit() + 1)
                .fetch()
                .map(JooqNotificationRepository::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int counts(NotificationCondition condition) {
        var q = dsl.selectCount()
                .from(NOTIFICATIONS)
                .where(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()))
                .and(NOTIFICATIONS.DELETED_AT.isNull());
        if (condition.isCountUnread()) {
            q.and(NOTIFICATIONS.READ.eq((short) 0));
        }
        return q.fetchOne(0, int.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateRead(NotificationCondition condition) {
        var q = dsl.update(NOTIFICATIONS)
                .set(NOTIFICATIONS.READ, (short) 1)
                .set(NOTIFICATIONS.UPDATED_AT, LocalDateTime.now())
                .where(NOTIFICATIONS.DELETED_AT.isNull());
        if (condition.getId() != null) {
            // 単一通知の更新は他staffの通知を更新できないよう staff_id でも絞り込む。
            q.and(NOTIFICATIONS.ID.eq(condition.getId()));
            q.and(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()));
        } else if (condition.getStaffId() != null && condition.isAll()) {
            q.and(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()));
        } else if (condition.getStaffId() != null && !condition.getIds().isEmpty()) {
            q.and(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()));
            q.and(NOTIFICATIONS.ID.in(condition.getIds()));
        }
        return q.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Notification entity) {
        var r = dsl.newRecord(NOTIFICATIONS);
        fillRecord(r, entity);
        r.store();
        entity.setId(r.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertBatch(List<Notification> entities) {
        var records = entities.stream().map(entity -> {
            var r = dsl.newRecord(NOTIFICATIONS);
            fillRecord(r, entity);
            return r;
        }).toList();
        dsl.batchInsert(records).execute();
    }

    /**
     * エンティティの値をjOOQレコードへ設定します。
     *
     * @param r jOOQレコード
     * @param entity 通知エンティティ
     */
    private static void fillRecord(com.authorization.jooq.tables.records.NotificationsRecord r, Notification entity) {
        r.setStaffId(entity.getStaffId());
        r.setMessageType((long) entity.getMessageType());
        r.setTitle(entity.getTitle());
        r.setMessage(entity.getMessage());
        r.setUrl(entity.getUrl());
        r.setRead((short) 0);
        r.setCreatedAt(entity.getCreatedAt());
        r.setCreatedBy(entity.getCreatedBy());
        r.setUpdatedAt(entity.getUpdatedAt());
        r.setUpdatedBy(entity.getUpdatedBy());
        r.setVersion((long) (entity.getVersion() != null ? entity.getVersion() : 1));
    }

    /**
     * カーソル文字列から通知IDを復号します。
     *
     * @param cursor カーソル文字列
     * @return 通知ID、復号できない場合は null
     */
    private static Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(cursor));
            String[] parts = decoded.split(",", 2);
            return parts.length == 2 ? Long.parseLong(parts[1]) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * jOOQレコードを通知エンティティへ変換します。
     *
     * @param rec jOOQレコード
     * @return 通知エンティティ
     */
    private static Notification toEntity(Record rec) {
        Notification n = new Notification();
        n.setId(rec.get(NOTIFICATIONS.ID));
        n.setStaffId(rec.get(NOTIFICATIONS.STAFF_ID));
        n.setMessageType(rec.get(NOTIFICATIONS.MESSAGE_TYPE).intValue());
        n.setTitle(rec.get(NOTIFICATIONS.TITLE));
        n.setMessage(rec.get(NOTIFICATIONS.MESSAGE));
        n.setUrl(rec.get(NOTIFICATIONS.URL));
        n.setRead(rec.get(NOTIFICATIONS.READ) != 0);
        n.setCreatedAt(rec.get(NOTIFICATIONS.CREATED_AT));
        n.setCreatedBy(rec.get(NOTIFICATIONS.CREATED_BY));
        n.setUpdatedAt(rec.get(NOTIFICATIONS.UPDATED_AT));
        n.setUpdatedBy(rec.get(NOTIFICATIONS.UPDATED_BY));
        n.setDeletedAt(rec.get(NOTIFICATIONS.DELETED_AT));
        n.setDeletedBy(rec.get(NOTIFICATIONS.DELETED_BY));
        n.setVersion(rec.get(NOTIFICATIONS.VERSION).intValue());
        return n;
    }
}
