<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http;

/**
 * staff_id クッキーの署名・検証を行うユーティリティクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http
 */
class StaffSession
{
    /**
     * staff_id クッキーの値を "{staffId}.{有効期限のUnix秒}.{HMAC-SHA256署名}" 形式で署名します。
     * secretを知らない第三者は staffId や有効期限を改ざんしても正しい署名を作成できないため、
     * クッキー値の改ざん（なりすまし）を防げます。有効期限も署名対象に含めることで、
     * Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を手動のCookieヘッダーで
     * 再送しても拒否できます。
     *
     * @param  int  $staffId  スタッフID
     * @param  string  $secret  署名用シークレット
     * @param  int  $lifetimeSeconds  有効期間（秒）
     * @return string 署名済みクッキー値
     */
    public static function sign(int $staffId, string $secret, int $lifetimeSeconds): string
    {
        $expiresAt = time() + $lifetimeSeconds;
        $payload = "{$staffId}.{$expiresAt}";

        return $payload.'.'.self::hmacHex($payload, $secret);
    }

    /**
     * 署名済み staff_id クッキーの値を検証し、staffIdを返します。
     * 署名が不正・形式不正・有効期限切れの場合は null（未認証）を返します。
     *
     * @param  string|null  $value  クッキー値
     * @param  string  $secret  署名用シークレット
     * @return int|null スタッフID、無効な場合はnull
     */
    public static function verify(?string $value, string $secret): ?int
    {
        if ($value === null || $value === '') {
            return null;
        }

        $parts = explode('.', $value, 3);
        if (count($parts) !== 3 || $parts[0] === '' || $parts[1] === '' || $parts[2] === '') {
            return null;
        }
        [$idPart, $expPart, $sig] = $parts;

        $payload = "{$idPart}.{$expPart}";
        if (!hash_equals(self::hmacHex($payload, $secret), $sig)) {
            return null;
        }

        if (!ctype_digit($idPart) || !ctype_digit($expPart)) {
            return null;
        }
        if (time() > (int) $expPart) {
            return null;
        }

        return (int) $idPart;
    }

    /**
     * HMAC-SHA256(payload, secret) の16進数文字列を返します。
     *
     * @param  string  $payload  署名対象の文字列
     * @param  string  $secret  署名用シークレット
     * @return string 16進数の署名文字列
     */
    private static function hmacHex(string $payload, string $secret): string
    {
        return hash_hmac('sha256', $payload, $secret);
    }
}
