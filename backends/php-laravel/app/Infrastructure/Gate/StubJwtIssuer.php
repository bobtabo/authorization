<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Gate;

use App\Domain\Gate\JwtIssuerInterface;

/**
 * JWT 発行を仮実装するStubクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Gate
 */
final class StubJwtIssuer implements JwtIssuerInterface
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function issueForMember(string $memberId): array
    {
        return [
            'message' => 'SUCCESS',
        ];
    }
}
