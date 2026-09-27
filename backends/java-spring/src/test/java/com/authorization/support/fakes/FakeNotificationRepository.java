/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.notification.condition.NotificationCondition;
import com.authorization.domain.notification.entities.Notification;
import com.authorization.domain.notification.repositories.NotificationRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * テスト用の手書きFake通知Repositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeNotificationRepository implements NotificationRepository {

    private final Map<Long, Notification> notifications = new LinkedHashMap<>();
    private final List<Notification> insertedBatch = new ArrayList<>();
    private long nextId = 1;

    /**
     * 通知を追加します。id未設定の場合は自動採番します。
     *
     * @param notification 追加する通知Entity
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeNotificationRepository add(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(nextId++);
        }
        notifications.put(notification.getId(), notification);
        return this;
    }

    /**
     * insertBatch でまとめて登録された通知の一覧を返します。
     *
     * @return 一括登録された通知一覧
     */
    public List<Notification> getInsertedBatch() {
        return insertedBatch;
    }

    /** {@inheritDoc} */
    @Override
    public List<Notification> listPage(NotificationCondition condition) {
        return notifications.values().stream()
                .filter(notification -> Objects.equals(notification.getStaffId(), condition.getStaffId()))
                .sorted(Comparator.comparing(Notification::getId).reversed())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public int counts(NotificationCondition condition) {
        return (int) notifications.values().stream()
                .filter(notification -> Objects.equals(notification.getStaffId(), condition.getStaffId()))
                .filter(notification -> !condition.isCountUnread() || !notification.isRead())
                .count();
    }

    /** {@inheritDoc} */
    @Override
    public int updateRead(NotificationCondition condition) {
        int updated = 0;
        for (Notification notification : notifications.values()) {
            if (matches(notification, condition) && !notification.isRead()) {
                notification.setRead(true);
                updated++;
            }
        }
        return updated;
    }

    /** {@inheritDoc} */
    @Override
    public void persist(Notification entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        notifications.put(entity.getId(), entity);
    }

    /** {@inheritDoc} */
    @Override
    public void insertBatch(List<Notification> entities) {
        for (Notification entity : entities) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            notifications.put(entity.getId(), entity);
        }
        insertedBatch.addAll(entities);
    }

    /**
     * 通知が更新条件に合致するかを判定します（単一通知/一括/ID指定の3パターン）。
     *
     * @param notification 判定対象の通知
     * @param condition 更新条件
     * @return 条件に合致する場合 true
     */
    private static boolean matches(Notification notification, NotificationCondition condition) {
        if (condition.getId() != null) {
            return notification.getId().equals(condition.getId())
                    && Objects.equals(notification.getStaffId(), condition.getStaffId());
        }
        if (condition.isAll()) {
            return Objects.equals(notification.getStaffId(), condition.getStaffId());
        }
        return Objects.equals(notification.getStaffId(), condition.getStaffId())
                && condition.getIds().contains(notification.getId());
    }
}
