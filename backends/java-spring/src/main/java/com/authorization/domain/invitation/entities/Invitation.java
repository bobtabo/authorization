/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.invitation.entities;

import com.authorization.support.entities.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 招待Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Invitation extends AbstractEntity {

    private Long id;
    private String token;
    private Integer role;
}
