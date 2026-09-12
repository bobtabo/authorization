/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.valueobjects;

import com.authorization.support.enums.SortType;
import com.authorization.support.valueobjects.AbstractValueObject;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * スタッフ一覧の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffListVo extends AbstractValueObject {

    private List<StaffResourceVo> items = List.of();
    private int count;
    private int offset;
    private int limit;
    private String sort = "";
    private SortType sortType = SortType.NONE;
}
