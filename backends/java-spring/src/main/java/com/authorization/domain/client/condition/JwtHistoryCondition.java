/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.condition;

import com.authorization.support.repositories.conditions.AbstractCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JWT履歴Conditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JwtHistoryCondition extends AbstractCondition {

    private Long clientId;
}
