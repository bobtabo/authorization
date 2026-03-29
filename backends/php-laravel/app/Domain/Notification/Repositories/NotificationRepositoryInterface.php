<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Notification\Repositories;

use App\Domain\Notification\Entities\Notification;

/**
 * 通知一覧のページング取得と件数集計を担うRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Repositories
 */
interface NotificationRepositoryInterface
{
    /**
     * @return array{items: list<Notification>, next_cursor: ?string}
     */
    public function listPage(?string $cursor, int $limit): array;

    /**
     * @return array<string, int>
     */
    public function counts(): array;
}
