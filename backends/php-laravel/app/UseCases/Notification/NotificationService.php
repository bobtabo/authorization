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
use App\Domain\Staff\Repositories\StaffRepository;
use App\Support\Exceptions\AppException;
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
     * @param StaffRepository $staffs スタッフRepository
     */
    public function __construct(
        private readonly NotificationRepository $notifications,
        private readonly StaffRepository $staffs,
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

        $page = $this->notifications->listPage((int)$dto->staffId, $dto->cursor, $limit);

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
        return (new NotificationCountsVo())->assign($this->notifications->counts((int)$dto->staffId));
    }

    /**
     * 一括既読などの更新を行います。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationBulkPatchVo 通知一括更新ValueObject
     */
    public function bulkMarkRead(NotificationDto $dto): NotificationBulkPatchVo
    {
        $updated = $this->notifications->bulkMarkRead((int)$dto->staffId, $dto->ids, $dto->all);

        return (new NotificationBulkPatchVo())->assign(['updated' => $updated]);
    }

    /**
     * 有効なスタッフ全員へ通知を配信します（ファンアウト）。
     *
     * @param string $title タイトル
     * @param string $message メッセージ
     * @param int $messageType メッセージ種類（1=info / 2=warn / 3=ok）
     * @param int $executorId 登録者ID
     * @param string|null $url 遷移先URL（省略可）
     * @return void
     */
    public function fanOut(string $title, string $message, int $messageType, int $executorId, ?string $url = null): void
    {
        $staffs = $this->staffs->findAllActive();
        foreach ($staffs as $staff) {
            $this->notifications->store($staff->id, $messageType, $title, $message, $executorId, $url);
        }
    }

    /**
     * 単一通知を部分更新します。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationPatchVo 通知更新ValueObject
     */
    public function patch(NotificationDto $dto): NotificationPatchVo
    {
        $id = $dto->notificationId;
        $ok = $this->notifications->patch((int)$id, $dto->attributes);
        if (!$ok) {
            throw AppException::noFound('notification_not_found');
        }

        return (new NotificationPatchVo())->assign(['ok' => true, 'id' => $id]);
    }
}
