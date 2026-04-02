<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Notification;

use App\Domain\Notification\Repositories\NotificationRepository;
use App\Domain\Notification\ValueObjects\NotificationBulkPatchVo;
use App\Domain\Notification\ValueObjects\NotificationCountsVo;
use App\Domain\Notification\ValueObjects\NotificationListVo;
use App\Domain\Notification\ValueObjects\NotificationPatchVo;
use App\Support\Services\AbstractService;
use App\UseCases\Notification\Dtos\NotificationDto;

/**
 * 通知一覧・件数取得・更新のユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification
 */
class NotificationService extends AbstractService
{
    /**
     * @param  NotificationRepository  $notifications  通知Repository
     */
    public function __construct(
        private readonly NotificationRepository $notifications,
    ) {}

    /**
     * カーソル付きで通知一覧ページを取得します。
     */
    public function listPage(NotificationDto $dto): NotificationListVo
    {
        $limit = $dto->limit;
        if ($limit < 1) {
            $limit = 1;
        }
        if ($limit > 100) {
            $limit = 100;
        }

        $page = $this->notifications->listPage($dto->cursor, $limit);
        $vo = new NotificationListVo;
        $vo->assignPage($page);

        return $vo;
    }

    /**
     * 通知件数の集計を取得します。
     */
    public function counts(NotificationDto $dto): NotificationCountsVo
    {
        unset($dto);
        $vo = new NotificationCountsVo;
        $vo->assignCounts($this->notifications->counts());

        return $vo;
    }

    /**
     * 一括既読などの更新を行います。
     */
    public function bulkMarkRead(NotificationDto $dto): NotificationBulkPatchVo
    {
        $vo = new NotificationBulkPatchVo;
        $vo->updated = $this->notifications->bulkMarkRead($dto->ids, $dto->all);

        return $vo;
    }

    /**
     * 単一通知を部分更新します。
     */
    public function patch(NotificationDto $dto): NotificationPatchVo
    {
        $vo = new NotificationPatchVo;
        $id = $dto->notificationId;
        if (! is_string($id) || $id === '') {
            return $vo;
        }

        $ok = $this->notifications->patch($id, $dto->attributes);
        $vo->ok = $ok;
        $vo->id = $id;

        return $vo;
    }
}
