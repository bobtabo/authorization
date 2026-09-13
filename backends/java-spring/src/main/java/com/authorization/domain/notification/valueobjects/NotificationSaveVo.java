/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.valueobjects;

import com.authorization.support.valueobjects.AbstractValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知更新ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationSaveVo extends AbstractValueObject {

    private boolean ok;
    private String message = "SUCCESS";
    private long id;
    private int updated;
}
