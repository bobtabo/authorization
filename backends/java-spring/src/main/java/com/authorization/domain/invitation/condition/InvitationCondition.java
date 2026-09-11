/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.invitation.condition;

import com.authorization.support.repositories.conditions.AbstractCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 招待Conditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvitationCondition extends AbstractCondition {

    private String token;
}
