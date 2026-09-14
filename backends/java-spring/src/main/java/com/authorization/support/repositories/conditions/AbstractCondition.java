/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.repositories.conditions;

import java.util.List;
import lombok.Data;

/**
 * 検索値を管理する基底クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
public class AbstractCondition {

    private Option option;
    private Long id;
    private List<Long> ids = List.of();

    /**
     * ページング可能であるか確認します。
     *
     * @return ページング可能な場合 true
     */
    public boolean isPaging() {
        if (option == null) {
            return false;
        }
        return option.getOffset() != null && option.getLimit() != null;
    }

    /**
     * 並び順に設定されているか確認します。
     *
     * @param column 対象カラム
     * @return 設定されている場合 true
     */
    public boolean isOrderBy(String column) {
        if (option == null) {
            return false;
        }
        return option.isOrderColumn(column);
    }
}
