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
     *
     * @param  string|null  $cursor  未使用（スタブ）
     * @param  int  $limit  未使用（スタブ）
     * @return array{items: list<never>, next_cursor: null} 空一覧
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
     *
     * @return array<string, int> 件数ゼロのスタブ
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
