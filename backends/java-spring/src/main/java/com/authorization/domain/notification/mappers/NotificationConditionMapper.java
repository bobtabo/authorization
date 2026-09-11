/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.mappers;

import com.authorization.domain.notification.condition.NotificationCondition;
import com.authorization.usecases.notification.dtos.NotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 通知 DTO を Condition へ変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface NotificationConditionMapper {

    /**
     * 通知 DTO を検索条件へ変換します。{@code notificationId} は明示的に {@code id} へ対応させます。
     *
     * @param dto 通知 DTO
     * @return 通知検索条件
     */
    @Mapping(target = "id", source = "notificationId")
    NotificationCondition toCondition(NotificationDto dto);
}
