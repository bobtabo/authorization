<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Gate;

/**
 * クライアント会員向け JWT 発行を抽象化するJwtIssuerインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Gate
 */
interface JwtIssuerInterface
{
    /**
     * 会員ID に紐づく JWT を発行します。
     *
     * @param  string  $memberId  クライアント会員ID
     * @return array{message: string} 発行処理結果
     */
    public function issueForMember(string $memberId): array;
}
