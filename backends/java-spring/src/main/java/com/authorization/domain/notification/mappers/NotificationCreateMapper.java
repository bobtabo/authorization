/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.mappers;

import com.authorization.domain.notification.entities.Notification;
import com.authorization.usecases.notification.dtos.NotificationCreateDto;
import org.mapstruct.Mapper;

/**
 * 通知登録 DTO の値を通知 Entity へ反映する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface NotificationCreateMapper {

    /**
     * 通知登録DTOから通知Entityを新規に組み立てます。staffId は宛先ごとに個別設定するため
     * 対象外です。
     *
     * @param dto 通知登録DTO
     * @return 通知Entity（未保存）
     */
    Notification toEntity(NotificationCreateDto dto);
}
