/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.dtos;

import com.authorization.support.enums.SortType;
import lombok.Getter;
import lombok.Setter;

/**
 * ページングDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public abstract class PagerDto extends AbstractDto {

    /** 1ページあたりのデフォルト取得件数。 */
    public static final int DEFAULT_LIMIT = 10;

    private int offset;
    private int limit;
    private String sort = "";
    private SortType sortType = SortType.NONE;

    /**
     * ページ番号からオフセットを算出して設定します。
     *
     * @param page ページ番号
     */
    public void setPaging(int page) {
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        this.offset = limit * (page - 1);
    }
}
