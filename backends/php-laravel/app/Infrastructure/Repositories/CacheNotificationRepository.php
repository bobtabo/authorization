<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Notification\Repositories\NotificationRepository;
use App\Support\Repositories\AbstractCacheRepository;

/**
 * 通知キャッシュRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class CacheNotificationRepository extends AbstractCacheRepository implements NotificationRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function listPage(int $staffId, ?string $cursor, int $limit): array
    {
        // TODO: Implement listPage() method.
        return ['items' => [], 'next_cursor' => null];
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function counts(int $staffId): array
    {
        // TODO: Implement counts() method.
        return ['unread' => 0, 'total' => 0, 'counts' => []];
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function bulkMarkRead(int $staffId, array $ids, bool $all): int
    {
        // TODO: Implement bulkMarkRead() method.
        return 0;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function patch(int $id, array $attributes): bool
    {
        // TODO: Implement patch() method.
        return true;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function store(int $staffId, int $messageType, string $title, string $message, int $executorId): void
    {
        // TODO: Implement store() method.
    }
}
