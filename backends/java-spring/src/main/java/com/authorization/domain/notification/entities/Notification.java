/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.notification.entities;

import com.authorization.support.entities.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends AbstractEntity {

    private Long id;
    private Long staffId;
    private Integer messageType;
    private String title;
    private String message;
    private String url;
    private boolean read;
}
