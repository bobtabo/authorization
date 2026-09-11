/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.enums;

/**
 * スタッフ権限を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public enum StaffRole {
    Administrator(1),
    Member(2);

    private final int value;

    StaffRole(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    /**
     * 整数値から権限を取得します。未知の値は Member を返します。
     *
     * @param value 権限整数値
     * @return 権限
     */
    public static StaffRole from(int value) {
        return value == Administrator.value ? Administrator : Member;
    }

    /**
     * 有効な権限値かどうかを判定します。
     *
     * @param value 権限整数値
     * @return Administrator/Member のいずれかであれば true
     */
    public static boolean isValid(int value) {
        return value == Administrator.value || value == Member.value;
    }
}
