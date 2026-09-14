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
 * スマホアプリ連携用QRコードValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientQrVo extends AbstractValueObject {

    private String identifier;
    private String deeplinkUrl;
}
