/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.enums;

/**
 * スタッフの状態を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public enum StaffStatus {
    Inactive(0),
    Active(1);

    private final int value;

    StaffStatus(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
