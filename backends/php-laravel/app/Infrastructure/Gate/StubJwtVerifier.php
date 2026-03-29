<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Gate;

use App\Domain\Gate\JwtVerifierInterface;

/**
 * JWT 検証を仮実装するStubクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Gate
 */
final class StubJwtVerifier implements JwtVerifierInterface
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function verify(string $identifier, string $token): array
    {
        return [
            'iss' => 'sii-auth',
            'sub' => '',
            'aud' => $identifier,
            'exp' => 0,
            'iat' => 0,
            'nbf' => 0,
            'jti' => '00000000-0000-0000-0000-000000000000',
        ];
    }
}
