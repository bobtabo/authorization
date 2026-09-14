/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.valueobjects;

import com.authorization.support.valueobjects.AbstractValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * クライアント利用開始ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientStartVo extends AbstractValueObject {

    private String accessToken;
}
