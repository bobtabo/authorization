/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.enums;

/**
 * ソート種別Enumクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public enum SortType {
    NONE(""),
    ASC("ASC"),
    DESC("DESC");

    private final String value;

    SortType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * 説明を取得します。
     *
     * @return 説明
     */
    public String description() {
        return switch (this) {
            case NONE -> "なし";
            case ASC -> "昇順";
            case DESC -> "降順";
        };
    }

    /**
     * 文字列からSortTypeを取得します。大文字小文字は区別しません。
     *
     * @param value 文字列（ASC/DESC/空）
     * @return SortType。一致しない場合は NONE
     */
    public static SortType fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return NONE;
        }
        return switch (value.toUpperCase()) {
            case "ASC" -> ASC;
            case "DESC" -> DESC;
            default -> NONE;
        };
    }
}
