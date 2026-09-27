/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.valueobjects;

import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.support.enums.SortType;
import com.authorization.support.valueobjects.AbstractValueObject;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JWT履歴一覧ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JwtHistoryListVo extends AbstractValueObject {

    private List<JwtHistory> items = List.of();
    private int count;
    private int offset;
    private int limit;
    private String sort = "";
    private SortType sortType = SortType.NONE;
}
