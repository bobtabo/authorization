/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.requests;

import java.util.regex.Pattern;

/**
 * クライアント登録・更新リクエストの検証クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public final class ClientValidation {

    private static final Pattern TEL_PATTERN = Pattern.compile("\\d{10,11}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ClientValidation() {
    }

    /**
     * クライアント登録リクエストを検証します。すべて必須項目です。
     *
     * @param name クライアント名
     * @param postCode 郵便番号
     * @param pref 都道府県
     * @param city 市区町村
     * @param address 丁目・番地
     * @param building ビル名
     * @param tel 電話番号
     * @param email メールアドレス
     * @return 検証成功の場合 true
     */
    public static boolean validateStore(String name, String postCode, String pref, String city, String address,
            String building, String tel, String email) {
        return isValid(name, 255) && isValid(postCode, 8) && isValid(pref, 50) && isValid(city, 100)
                && isValid(address, 255) && (building == null || building.length() <= 255)
                && tel != null && TEL_PATTERN.matcher(tel).matches()
                && email != null && EMAIL_PATTERN.matcher(email).matches() && email.length() <= 255;
    }

    /**
     * クライアント更新リクエストを検証します。値が設定されている項目のみ検証します。
     *
     * @param name クライアント名
     * @param postCode 郵便番号
     * @param pref 都道府県
     * @param city 市区町村
     * @param address 丁目・番地
     * @param building ビル名
     * @param tel 電話番号
     * @param email メールアドレス
     * @return 検証成功の場合 true
     */
    public static boolean validateUpdate(String name, String postCode, String pref, String city, String address,
            String building, String tel, String email) {
        return maxLenOrNull(name, 255) && maxLenOrNull(postCode, 8) && maxLenOrNull(pref, 50)
                && maxLenOrNull(city, 100) && maxLenOrNull(address, 255) && maxLenOrNull(building, 255)
                && (tel == null || TEL_PATTERN.matcher(tel).matches())
                && (email == null || (EMAIL_PATTERN.matcher(email).matches() && email.length() <= 255));
    }

    /**
     * 必須項目の文字列を検証します。
     *
     * @param s 検証対象の文字列
     * @param maxLen 最大文字数
     * @return 空でなく最大文字数以内の場合 true
     */
    private static boolean isValid(String s, int maxLen) {
        return s != null && !s.isEmpty() && s.length() <= maxLen;
    }

    /**
     * 省略可能な文字列を検証します。
     *
     * @param s 検証対象の文字列
     * @param maxLen 最大文字数
     * @return null、または最大文字数以内の場合 true
     */
    private static boolean maxLenOrNull(String s, int maxLen) {
        return s == null || s.length() <= maxLen;
    }
}
