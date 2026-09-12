/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.enums;

/**
 * クライアントの状態を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public enum ClientStatus {
    Pending(0),
    Inactive(1),
    Active(2),
    Suspended(3),
    Closed(4);

    private final int value;

    /**
     * コンストラクタ。
     *
     * @param value 状態整数値
     */
    ClientStatus(int value) {
        this.value = value;
    }

    /**
     * 状態整数値を返します。
     *
     * @return 状態整数値
     */
    public int value() {
        return value;
    }

    /**
     * 整数値から状態を取得します。
     *
     * @param value 状態整数値
     * @return 状態
     */
    public static ClientStatus from(int value) {
        for (ClientStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("unknown client status: " + value);
    }
}
