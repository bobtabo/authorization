/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.repositories.conditions;

import com.authorization.support.enums.SortType;
import lombok.Getter;

/**
 * オプションクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
public class Option {

    private Integer offset;
    private Integer limit;
    private String orderBy;
    private String orderByDesc;

    /**
     * コンストラクタ。
     *
     * @param offset オフセット
     * @param limit リミット
     * @param sort ソート対象
     * @param sortType ソート種類
     */
    public Option(Integer offset, Integer limit, String sort, SortType sortType) {
        this.offset = offset;
        this.limit = limit;
        this.orderBy = sortType == SortType.ASC ? sort : null;
        this.orderByDesc = sortType == SortType.DESC ? sort : null;

        if (limit != null && limit < 0) {
            this.offset = null;
            this.limit = null;
        }
    }

    /**
     * 並び順が設定されているか確認します。
     *
     * @return 設定されている場合 true
     */
    public boolean hasOrderBy() {
        return (orderBy != null && !orderBy.isEmpty()) || (orderByDesc != null && !orderByDesc.isEmpty());
    }

    /**
     * 対象カラムが並び順に設定されているか確認します。
     *
     * @param column 対象カラム
     * @return 設定されている場合 true
     */
    public boolean isOrderColumn(String column) {
        if (!hasOrderBy()) {
            return false;
        }
        return column.equals(orderBy) || column.equals(orderByDesc);
    }
}
