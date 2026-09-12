/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.repositories;

import com.authorization.domain.notification.condition.NotificationCondition;
import com.authorization.domain.notification.entities.Notification;
import java.util.List;

/**
 * 通知Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public interface NotificationRepository {

    /**
     * カーソル付きで通知一覧ページを取得します。
     *
     * @param condition 検索条件
     * @return エンティティ一覧
     */
    List<Notification> listPage(NotificationCondition condition);

    /**
     * 通知件数の集計を取得します。
     *
     * @param condition 検索条件
     * @return 件数
     */
    int counts(NotificationCondition condition);

    /**
     * 通知を既読更新します。
     *
     * @param condition 検索条件
     * @return 更新件数
     */
    int updateRead(NotificationCondition condition);

    /**
     * 通知を登録します。
     *
     * @param entity エンティティ
     */
    void persist(Notification entity);

    /**
     * 通知を一括登録します。
     *
     * @param entities エンティティ一覧
     */
    void insertBatch(List<Notification> entities);
}
