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
     * @return array{message: string}
     */
    public function issueForMember(string $memberId): array;
}
