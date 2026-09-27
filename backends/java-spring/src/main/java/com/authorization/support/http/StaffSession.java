/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.http;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * staff_id クッキーの署名・検証を行うユーティリティです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public final class StaffSession {

    private StaffSession() {
    }

    /**
     * staff_id クッキーの値を "{staffId}.{有効期限のUnix秒}.{HMAC-SHA256署名}" 形式で署名します。
     * secretを知らない第三者は staffId や有効期限を改ざんしても正しい署名を作成できないため、
     * クッキー値の改ざん（なりすまし）を防げます。有効期限も署名対象に含めることで、
     * Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を手動のCookieヘッダーで
     * 再送しても拒否できます。
     *
     * @param staffId スタッフID
     * @param secret 署名用シークレット
     * @param lifetimeSeconds 有効期間（秒）
     * @return 署名済みクッキー値
     */
    public static String signStaffId(long staffId, String secret, long lifetimeSeconds) {
        long expiresAt = System.currentTimeMillis() / 1000 + lifetimeSeconds;
        String payload = staffId + "." + expiresAt;
        return payload + "." + hmacHex(payload, secret);
    }

    /**
     * 署名済み staff_id クッキーの値を検証し、staffIdを返します。
     * 署名が不正・形式不正・有効期限切れの場合は 0（未認証）を返します。
     *
     * @param value クッキー値
     * @param secret 署名用シークレット
     * @return staffId、無効な場合は0
     */
    public static long verifyStaffId(String value, String secret) {
        if (value == null) return 0;
        String[] parts = value.split("\\.", 3);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) return 0;

        String payload = parts[0] + "." + parts[1];
        String expectedSig = hmacHex(payload, secret);
        if (!MessageDigest.isEqual(
                parts[2].getBytes(StandardCharsets.UTF_8), expectedSig.getBytes(StandardCharsets.UTF_8))) {
            return 0;
        }

        long id;
        long expiresAt;
        try {
            id = Long.parseLong(parts[0]);
            expiresAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return 0;
        }
        if (System.currentTimeMillis() / 1000 > expiresAt) return 0;

        return id;
    }

    /**
     * HMAC-SHA256(payload, secret) の16進数文字列を返します。
     *
     * @param payload 署名対象の文字列
     * @param secret 署名用シークレット
     * @return 16進数の署名文字列
     */
    private static String hmacHex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
