/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.dtos;

import lombok.Data;

/**
 * 基底DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
public abstract class AbstractDto {

    private Long executorId;
    private Integer version;
}
