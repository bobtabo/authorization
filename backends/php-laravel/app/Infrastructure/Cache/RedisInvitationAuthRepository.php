<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Cache;

use App\Domain\Invitation\Repositories\InvitationAuthRepository;
use App\Support\Repositories\AbstractCacheRepository;

/**
 * 招待認証Repositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Cache
 */
class RedisInvitationAuthRepository extends AbstractCacheRepository implements InvitationAuthRepository
{
    private const string TAG = 'invitation_auth';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function store(string $token, int $ttl): void
    {
        $this->put(self::TAG, self::TAG . ':' . $token, $token, $ttl);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function find(string $token): ?string
    {
        $value = $this->get(self::TAG, self::TAG . ':' . $token);
        return is_string($value) ? $value : null;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function remove(string $token): void
    {
        $this->delete(self::TAG, self::TAG . ':' . $token);
    }
}
