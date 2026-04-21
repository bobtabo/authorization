<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace app\Infrastructure\Repositories;

use App\Domain\Gate\Repositories\GateRepository;
use App\Support\Repositories\AbstractCacheRepository;

/**
 * Gate（JWT）向けキャッシュRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Gate
 */
class CacheGateRepository extends AbstractCacheRepository implements GateRepository
{
    private const TAG = 'gate.jwt';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function putJwt(string $identifier, string $memberId, string $token, int $ttl): void
    {
        $this->put(self::TAG, "{$identifier}.{$memberId}", $token, $ttl);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function getJwt(string $identifier, string $memberId): ?string
    {
        $value = $this->get(self::TAG, "{$identifier}.{$memberId}");
        return is_string($value) ? $value : null;
    }
}
