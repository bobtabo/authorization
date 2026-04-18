<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Notification;

use App\Domain\Notification\Condition\NotificationCondition;
use App\Domain\Notification\Entities\Notification;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Domain\Notification\ValueObjects\NotificationCountsVo;
use App\Domain\Notification\ValueObjects\NotificationListVo;
use App\Domain\Notification\ValueObjects\NotificationSaveVo;
use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Support\Exceptions\AppException;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\Support\Traits\EnumValue;
use app\UseCases\Notification\Dtos\NotificationCreateDto;
use App\UseCases\Notification\Dtos\NotificationDto;

/**
 * 通知Serviceクラスです。
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
        $condition = SimpleMapper::map($dto, NotificationCondition::class);

        $list = $this->notificationRepository->listPage($condition);
        $condition->limit = max(1, min(100, $dto->limit));
        $hasNext = $list->count() > $condition->limit;
        $items = $hasNext ? $list->slice(0, $condition->limit) : $list;

        $nextCursor = null;
        if ($hasNext) {
            $last = $items->last();
            $nextCursor = base64_encode($last->createdAt->format('Y-m-d H:i:s') . ',' . $last->id);
        }

        $values = $list->map(function (Notification $row) use ($dto) {
            return $this->toValues($row->attributesBySnake());
        });

        return new NotificationListVo()->assign([
            'items' => $values->all(),
            'nextCursor' => $nextCursor,
        ]);
    }

    /**
     * 通知件数の集計を取得します。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationCountsVo 通知件数ValueObject
     */
    public function counts(NotificationDto $dto): NotificationCountsVo
    {
        /** @var NotificationCondition $condition */
        $condition = SimpleMapper::map($dto, NotificationCondition::class);
        $total = $this->notificationRepository->counts($condition);

        $condition->countUnread = true;
        $unread = $this->notificationRepository->counts($condition);

        return new NotificationCountsVo()->assign([
            'total' => $total,
            'unread' => $unread,
        ]);
    }

    /**
     * 一括既読などの更新を行います。
     *
     * @param NotificationDto $dto 通知DTO
     * @return NotificationSaveVo 通知更新ValueObject
     */
    public function reads(NotificationDto $dto): NotificationSaveVo
    {
        $condition = SimpleMapper::map($dto, NotificationCondition::class);
        $updated = $this->notificationRepository->updateRead($condition);

        return new NotificationSaveVo()->assign(['updated' => $updated]);
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
     * @return NotificationSaveVo 通知更新ValueObject
     */
    public function read(NotificationDto $dto): NotificationSaveVo
    {
        $condition = SimpleMapper::mapSpecific($dto, NotificationCondition::class, [
            'notificationId' => 'id',
        ]);

        $result = $this->notificationRepository->updateRead($condition);
        if (!$result) {
            throw AppException::notFound('notification_not_found');
        }

        return new NotificationSaveVo()->assign([
            'ok' => true,
            'id' => $dto->notificationId
        ]);
    }
}
