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
     * クライアント識別子とトークンで JWT を検証します。
     *
     * @param  string  $identifier  クライアント識別名
     * @param  string  $token  JWT 文字列
     * @return array<string, mixed> 検証結果（Payload 相当）
     */
    public function verify(string $identifier, string $token): array;
}
