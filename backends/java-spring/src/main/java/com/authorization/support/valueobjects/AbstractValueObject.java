/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.valueobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 基底ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
public abstract class AbstractValueObject {

    @JsonIgnore
    private Integer version;
}
