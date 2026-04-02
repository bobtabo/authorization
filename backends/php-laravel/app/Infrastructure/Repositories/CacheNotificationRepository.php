<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
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
    public function listPage(?string $cursor, int $limit): array
    {
        // TODO: Implement listPage() method.
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function counts(): array
    {
        // TODO: Implement counts() method.
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function bulkMarkRead(?array $ids, bool $all): int
    {
        // TODO: Implement bulkMarkRead() method.
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function patch(string $id, array $attributes): bool
    {
        // TODO: Implement patch() method.
    }
}
