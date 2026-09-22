// This is a program developed by BobTabo.
//
// Copyright (c) 2026 BobTabo. All Rights Reserved.
using System.Security.Cryptography;
using System.Text;

namespace Authorization.Api.Handler;

/// <summary>staff_id クッキーの署名・検証です。</summary>
public static class StaffSession
{
    /// <summary>
    /// staff_id クッキーの値を "{staffId}.{有効期限のUnix秒}.{HMAC-SHA256署名}" 形式で署名します。
    /// secretを知らない第三者は staffId や有効期限を改ざんしても正しい署名を作成できないため、
    /// クッキー値の改ざん（なりすまし）を防げます。有効期限も署名対象に含めることで、
    /// Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を手動のCookieヘッダーで
    /// 再送しても拒否できます。
    /// </summary>
    /// <param name="staffId">スタッフID</param>
    /// <param name="secret">署名用シークレット</param>
    /// <param name="lifetime">有効期間</param>
    /// <returns>署名済みクッキー値</returns>
    public static string SignStaffId(long staffId, string secret, TimeSpan lifetime)
    {
        var expiresAt = DateTimeOffset.UtcNow.Add(lifetime).ToUnixTimeSeconds();
        var payload = $"{staffId}.{expiresAt}";
        return $"{payload}.{HmacHex(payload, secret)}";
    }

    /// <summary>
    /// 署名済み staff_id クッキーの値を検証し、staffIdを返します。
    /// 署名が不正・形式不正・有効期限切れの場合は 0（未認証）を返します。
    /// </summary>
    /// <param name="value">クッキー値</param>
    /// <param name="secret">署名用シークレット</param>
    /// <returns>staffId、無効な場合は0</returns>
    public static long VerifyStaffId(string value, string secret)
    {
        var parts = value.Split('.', 3);
        if (parts.Length != 3 || parts[0].Length == 0 || parts[1].Length == 0 || parts[2].Length == 0) return 0;

        var payload = $"{parts[0]}.{parts[1]}";
        var expectedSig = HmacHex(payload, secret);
        if (!CryptographicOperations.FixedTimeEquals(Encoding.UTF8.GetBytes(parts[2]), Encoding.UTF8.GetBytes(expectedSig)))
            return 0;

        if (!long.TryParse(parts[0], out var id)) return 0;
        if (!long.TryParse(parts[1], out var expiresAt)) return 0;
        if (DateTimeOffset.UtcNow.ToUnixTimeSeconds() > expiresAt) return 0;

        return id;
    }

    /// <summary>HMAC-SHA256(payload, secret) の16進数文字列を返します。</summary>
    /// <param name="payload">署名対象の文字列</param>
    /// <param name="secret">署名用シークレット</param>
    /// <returns>16進数の署名文字列</returns>
    private static string HmacHex(string payload, string secret)
    {
        using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret));
        var hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(payload));
        return Convert.ToHexStringLower(hash);
    }
}
