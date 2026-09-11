/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.valueobjects;

import com.authorization.support.valueobjects.AbstractValueObject;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知件数ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationCountsVo extends AbstractValueObject {

    private int unread;
    private int total;
    private Map<String, Integer> counts = Map.of();
}
