/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.notification.dtos;

import com.authorization.support.dtos.AbstractDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知登録DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class NotificationCreateDto extends AbstractDto {

    private Integer messageType;
    private String title;
    private String message;
    private String url;
}
