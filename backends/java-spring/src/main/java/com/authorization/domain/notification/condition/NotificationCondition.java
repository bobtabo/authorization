/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.condition;

import com.authorization.support.repositories.conditions.AbstractCondition;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知Conditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationCondition extends AbstractCondition {

    private Long staffId;
    private Integer messageType;
    private String title;
    private String message;
    private boolean read;
    private boolean countUnread;
    private String cursor;
    private int limit = 1;
    private List<Long> ids = List.of();
    private boolean all;
}
