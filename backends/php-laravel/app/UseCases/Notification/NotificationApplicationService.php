<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Notification;

use App\Domain\Notification\Entities\Notification;
use App\Domain\Notification\Repositories\NotificationRepositoryInterface;
use App\UseCases\Common\AbstractService;

/**
 * 通知一覧・件数取得のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification
 */
final class NotificationApplicationService extends AbstractService
{
    /**
     * @param  NotificationRepositoryInterface  $notifications  通知Repository
     */
    public function __construct(
        private readonly NotificationRepositoryInterface $notifications,
    ) {}

    /**
     * カーソル付きで通知一覧ページを取得します。
     *
     * @param  string|null  $cursor  次ページカーソル（先頭は null）
     * @param  int  $limit  1ページあたりの最大件数
     * @return array{items: list<Notification>, next_cursor: ?string} 一覧と次カーソル
     */
    public function listPage(?string $cursor, int $limit): array
    {
        return $this->notifications->listPage($cursor, $limit);
    }

    /**
     * 通知件数の集計を取得します。
     *
     * @return array<string, int> 種別ごとの件数
     */
    public function counts(): array
    {
        return $this->notifications->counts();
    }
}
