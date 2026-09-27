/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.valueobjects;

import com.authorization.support.valueobjects.AbstractValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * スタッフ権限更新の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffMutationVo extends AbstractValueObject {

    private boolean ok;
    private String message = "SUCCESS";
    private Long id;
}
