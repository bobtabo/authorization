package com.authorization.handler

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * staff_id クッキーの値を "{staffId}.{有効期限のUnix秒}.{HMAC-SHA256署名}" 形式で署名します。
 * secretを知らない第三者は staffId や有効期限を改ざんしても正しい署名を作成できないため、
 * クッキー値の改ざん（なりすまし）を防げます。有効期限も署名対象に含めることで、
 * Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を手動のCookieヘッダーで
 * 再送しても拒否できます。
 */
fun signStaffId(staffId: Long, secret: String, lifetimeSeconds: Long): String {
    val expiresAt = System.currentTimeMillis() / 1000 + lifetimeSeconds
    val payload = "$staffId.$expiresAt"
    return "$payload.${hmacHex(payload, secret)}"
}

/**
 * 署名済み staff_id クッキーの値を検証し、staffIdを返します。
 * 署名が不正・形式不正・有効期限切れの場合は 0（未認証）を返します。
 */
fun verifyStaffId(value: String?, secret: String): Long {
    if (value.isNullOrEmpty()) return 0
    val parts = value.split(".", limit = 3)
    if (parts.size != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) return 0

    val payload = "${parts[0]}.${parts[1]}"
    val expectedSig = hmacHex(payload, secret)
    if (!MessageDigest.isEqual(parts[2].toByteArray(), expectedSig.toByteArray())) return 0

    val id = parts[0].toLongOrNull() ?: return 0
    val expiresAt = parts[1].toLongOrNull() ?: return 0
    if (System.currentTimeMillis() / 1000 > expiresAt) return 0

    return id
}

/** HMAC-SHA256(payload, secret) の16進数文字列を返します。 */
private fun hmacHex(payload: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
    return mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
}
