<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Notification;

use App\Domain\Notification\Entities\Notification;
use App\Domain\Notification\Repositories\NotificationRepositoryInterface;

/**
 * 通知一覧・件数取得のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification
 */
final class NotificationApplicationService
{
    public function __construct(
        private readonly NotificationRepositoryInterface $notifications,
    ) {}

    /**
     * @return array{items: list<Notification>, next_cursor: ?string}
     */
    public function listPage(?string $cursor, int $limit): array
    {
        return $this->notifications->listPage($cursor, $limit);
    }

    /**
     * @return array<string, int>
     */
    public function counts(): array
    {
        return $this->notifications->counts();
    }
}
