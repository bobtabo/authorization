/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.enums;

/**
 * プロバイダーを表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public enum Provider {
    Google(1),
    Github(2);

    private final int value;

    /**
     * コンストラクタ。
     *
     * @param value プロバイダー整数値
     */
    Provider(int value) {
        this.value = value;
    }

    /**
     * プロバイダー整数値を返します。
     *
     * @return プロバイダー整数値
     */
    public int value() {
        return value;
    }

    /**
     * 整数値からプロバイダーを取得します。
     *
     * @param value プロバイダー整数値
     * @return プロバイダー
     */
    public static Provider from(int value) {
        for (Provider provider : values()) {
            if (provider.value == value) {
                return provider;
            }
        }
        throw new IllegalArgumentException("unknown provider: " + value);
    }
}
