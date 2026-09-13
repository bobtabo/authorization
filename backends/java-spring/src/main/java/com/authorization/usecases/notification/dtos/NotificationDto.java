/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.notification.dtos;

import com.authorization.support.dtos.AbstractDto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class NotificationDto extends AbstractDto {

    private Long staffId;
    private String cursor;
    private int limit = 10;

    /** 一括更新対象の通知 ID（空リストは ids 未指定）。 */
    private List<Long> ids = List.of();

    private boolean all;
    private Long notificationId;
}
