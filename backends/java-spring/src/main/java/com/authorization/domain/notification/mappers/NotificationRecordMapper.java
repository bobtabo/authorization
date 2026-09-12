/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.mappers;

import com.authorization.domain.notification.entities.Notification;
import com.authorization.jooq.tables.records.NotificationsRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 通知 Entity と jOOQ Record を相互変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface NotificationRecordMapper {

    /**
     * jOOQレコードを通知Entityへ変換します。
     *
     * @param rec jOOQレコード
     * @return 通知Entity
     */
    @Mapping(target = "read", expression = "java(rec.getRead() != null && rec.getRead() != 0)")
    Notification toEntity(NotificationsRecord rec);

    /**
     * 通知Entityの値をjOOQレコードへ設定します（新規登録用）。新規通知は常に未読（0）で
     * 登録するため、Entity側の read 値は使用しません。version 未設定時は 1 とします。
     *
     * @param entity 通知Entity
     * @param record jOOQレコード（書き込み先）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "read", expression = "java((short) 0)")
    @Mapping(
            target = "version",
            expression = "java((long) (entity.getVersion() != null ? entity.getVersion() : 1))")
    void fillRecord(Notification entity, @MappingTarget NotificationsRecord record);
}
