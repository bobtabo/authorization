<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Notification;

use App\Domain\Notification\Entities\Notification;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Domain\Notification\ValueObjects\NotificationBulkPatchVo;
use App\Domain\Notification\ValueObjects\NotificationCountsVo;
use App\Domain\Notification\ValueObjects\NotificationListVo;
use App\Domain\Notification\ValueObjects\NotificationPatchVo;
use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Support\Exceptions\AppException;
use App\Support\Services\AbstractService;
use App\Support\Traits\EnumValue;
use app\UseCases\Notification\Dtos\NotificationCreateDto;
use App\UseCases\Notification\Dtos\NotificationDto;

/**
 * 通知一覧・件数取得・更新のユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Notification
 */
class NotificationService extends AbstractService
{
    use EnumValue;

    /**
     * @param NotificationRepository $notificationRepository 通知Repository
     * @param StaffRepository $staffRepository スタッフRepository
     */
    public function __construct(
        private readonly NotificationRepository $notificationRepository,
        private readonly StaffRepository $staffRepository,
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

        $page = $this->notificationRepository->listPage((int)$dto->staffId, $dto->cursor, $limit);

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
        return (new NotificationCountsVo())->assign($this->notificationRepository->counts((int)$dto->staffId));
    }

    /**
     * 一括既読などの更新を行います。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationBulkPatchVo 通知一括更新ValueObject
     */
    public function bulkMarkRead(NotificationDto $dto): NotificationBulkPatchVo
    {
        $updated = $this->notificationRepository->bulkMarkRead((int)$dto->staffId, $dto->ids, $dto->all);

        return (new NotificationBulkPatchVo())->assign(['updated' => $updated]);
    }

    /**
     * 有効なスタッフ全員へ通知を配信します（ファンアウト）。
     *
     * @param NotificationCreateDto $dto 通知登録DTO
     * @return void
     */
    public function fanOut(NotificationCreateDto $dto): void
    {
        $staffs = $this->staffRepository->findAllActive();

        $notifications = $staffs->map(function (Staff $row) use ($dto) {
            $entity = new Notification();
            $entity->staffId = $row->id;
            $entity->messageType = $dto->messageType;
            $entity->title = $dto->title;
            $entity->message = $dto->message;
            $entity->url = $dto->url;
            $entity->assignCreated($dto->executorId);
            return $this->toValues($entity->attributesBySnake());
        });

        $this->notificationRepository->insertBatch($notifications->all());
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
        $ok = $this->notificationRepository->patch((int)$id, $dto->attributes);
        if (!$ok) {
            throw AppException::notFound('notification_not_found');
        }

        return (new NotificationPatchVo())->assign(['ok' => true, 'id' => $id]);
    }
}
