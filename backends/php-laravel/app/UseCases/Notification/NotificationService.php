<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

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
     * @param NotificationRepository $notifications 通知Repository
     */
    public function __construct(
        private readonly NotificationRepository $notifications,
    ) {
    }

    /**
     * カーソル付きで通知一覧ページを取得します。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationListVo 通知一覧ValueObject
     */
    public function listPage(NotificationDto $dto): NotificationListVo
    {
        $limit = max(1, min(100, $dto->limit));

        $page = $this->notifications->listPage($dto->cursor, $limit);

        return (new NotificationListVo())->assign($page);
    }

    /**
     * 通知件数の集計を取得します。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationCountsVo 通知件数ValueObject
     */
    public function counts(NotificationDto $dto): NotificationCountsVo
    {
        unset($dto);

        return (new NotificationCountsVo())->assign($this->notifications->counts());
    }

    /**
     * 一括既読などの更新を行います。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationBulkPatchVo 通知一括更新ValueObject
     */
    public function bulkMarkRead(NotificationDto $dto): NotificationBulkPatchVo
    {
        $updated = $this->notifications->bulkMarkRead($dto->ids, $dto->all);

        return (new NotificationBulkPatchVo())->assign(['updated' => $updated]);
    }

    /**
     * 単一通知を部分更新します。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationPatchVo 通知更新ValueObject
     */
    public function patch(NotificationDto $dto): NotificationPatchVo
    {
        $vo = new NotificationPatchVo();
        $id = $dto->notificationId;
        if (!is_string($id) || $id === '') {
            return $vo;
        }

        $ok = $this->notifications->patch($id, $dto->attributes);

        return $vo->assign(['ok' => $ok, 'id' => $id]);
    }
}
