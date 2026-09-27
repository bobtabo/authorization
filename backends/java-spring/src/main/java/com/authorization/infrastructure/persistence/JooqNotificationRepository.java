/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.NOTIFICATIONS;

import com.authorization.domain.notification.condition.NotificationCondition;
import com.authorization.domain.notification.entities.Notification;
import com.authorization.domain.notification.mappers.NotificationRecordMapper;
import com.authorization.domain.notification.repositories.NotificationRepository;
import com.authorization.jooq.tables.records.NotificationsRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使った通知Repositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqNotificationRepository implements NotificationRepository {

    private final DSLContext dsl;
    private final NotificationRecordMapper recordMapper;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     * @param recordMapper 通知 Entity/Record マッパー
     */
    public JooqNotificationRepository(DSLContext dsl, NotificationRecordMapper recordMapper) {
        this.dsl = dsl;
        this.recordMapper = recordMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> listPage(NotificationCondition condition) {
        Long afterId = decodeCursor(condition.getCursor());
        var query = dsl.selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()))
                .and(NOTIFICATIONS.DELETED_AT.isNull());
        if (afterId != null) {
            query.and(NOTIFICATIONS.ID.lessThan(afterId));
        }
        // カーソルページングでは +1 件多く取得し、次ページの有無を判定する（Service側の責務）。
        return query.orderBy(NOTIFICATIONS.ID.desc())
                .limit(condition.getLimit() + 1)
                .fetch()
                .map(recordMapper::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int counts(NotificationCondition condition) {
        var query = dsl.selectCount()
                .from(NOTIFICATIONS)
                .where(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()))
                .and(NOTIFICATIONS.DELETED_AT.isNull());
        if (condition.isCountUnread()) {
            query.and(NOTIFICATIONS.READ.eq((short) 0));
        }
        return query.fetchOne(0, int.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateRead(NotificationCondition condition) {
        var query = dsl.update(NOTIFICATIONS)
                .set(NOTIFICATIONS.READ, (short) 1)
                .set(NOTIFICATIONS.UPDATED_AT, LocalDateTime.now())
                .where(NOTIFICATIONS.DELETED_AT.isNull());
        if (condition.getId() != null) {
            // 単一通知の更新は他staffの通知を更新できないよう staff_id でも絞り込む。
            query.and(NOTIFICATIONS.ID.eq(condition.getId()));
            query.and(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()));
        } else if (condition.getStaffId() != null && condition.isAll()) {
            query.and(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()));
        } else if (condition.getStaffId() != null && !condition.getIds().isEmpty()) {
            query.and(NOTIFICATIONS.STAFF_ID.eq(condition.getStaffId()));
            query.and(NOTIFICATIONS.ID.in(condition.getIds()));
        }
        return query.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Notification entity) {
        NotificationsRecord notificationsRecord = dsl.newRecord(NOTIFICATIONS);
        recordMapper.fillRecord(entity, notificationsRecord);
        notificationsRecord.store();
        entity.setId(notificationsRecord.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertBatch(List<Notification> entities) {
        var records = entities.stream().map(entity -> {
            NotificationsRecord notificationsRecord = dsl.newRecord(NOTIFICATIONS);
            recordMapper.fillRecord(entity, notificationsRecord);
            return notificationsRecord;
        }).toList();
        dsl.batchInsert(records).execute();
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
}
