/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.gate.valueobjects;

import com.authorization.support.valueobjects.AbstractValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JWT検証ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GateVerifyVo extends AbstractValueObject {

    private String iss;
    private String sub;
    private String aud;
    private long exp;
    private long iat;
    private long nbf;
    private String jti;
}
