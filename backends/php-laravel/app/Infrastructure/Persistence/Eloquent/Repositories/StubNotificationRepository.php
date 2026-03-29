<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Notification\Repositories\NotificationRepositoryInterface;

/**
 * 永続化未接続時に通知一覧・件数を空で返すStubのRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
final class StubNotificationRepository implements NotificationRepositoryInterface
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function listPage(?string $cursor, int $limit): array
    {
        return [
            'items' => [],
            'next_cursor' => null,
        ];
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function counts(): array
    {
        return [
            'unread' => 0,
            'total' => 0,
        ];
    }
}
