/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.valueobjects;

import com.authorization.domain.notification.entities.Notification;
import com.authorization.support.valueobjects.AbstractValueObject;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知一覧ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationListVo extends AbstractValueObject {

    private List<Notification> items = List.of();
    private String nextCursor;
}
