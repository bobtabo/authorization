<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Gate;

/**
 * クライアント識別子とトークンによる JWT 検証を抽象化するJwtVerifierインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Gate
 */
interface JwtVerifierInterface
{
    /**
     * @return array<string, mixed>
     */
    public function verify(string $identifier, string $token): array;
}
