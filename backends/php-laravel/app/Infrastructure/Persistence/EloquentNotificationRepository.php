<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Persistence;

use App\Domain\Notification\Condition\NotificationCondition;
use App\Domain\Notification\Entities\Notification as Entity;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Infrastructure\Models\Notification as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Carbon\Carbon;
use Illuminate\Support\Collection;

/**
 * 通知Repositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence
 */
class EloquentNotificationRepository extends AbstractEloquentRepository implements NotificationRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function listPage(NotificationCondition $condition): Collection
    {
        $query = Model::query()
            ->where('staff_id', $condition->staffId)
            ->orderBy('created_at', 'desc')
            ->orderBy('id', 'desc');

        if ($condition->cursor !== null) {
            $decoded = base64_decode($condition->cursor, true);
            if ($decoded !== false && str_contains($decoded, ',')) {
                [$cursorCreatedAt, $cursorId] = explode(',', $decoded, 2);
                $query->where(function ($q) use ($cursorCreatedAt, $cursorId) {
                    $q->where('created_at', '<', $cursorCreatedAt)
                        ->orWhere(function ($q2) use ($cursorCreatedAt, $cursorId) {
                            $q2->where('created_at', '=', $cursorCreatedAt)
                                ->where('id', '<', (int) $cursorId);
                        });
                });
            }
        }

        $query->limit($condition->limit + 1);

        return $this->findByQuery($query);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function counts(NotificationCondition $condition): int
    {
        $query = $this->getModel()->newQuery()->where('staff_id', $condition->staffId);

        if ($condition->countUnread) {
            $query->where('read', false);
        }

        return $this->count($query);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function updateRead(NotificationCondition $condition): int
    {
        if ($condition->id !== null) {
            // staffId は呼び出し元（認証済みスタッフ）のものを維持する。対象通知の
            // staffId で上書きすると、他staffの通知IDを指定しても既読化できてしまう。
            $condition->ids[] = $condition->id;
        }

        $query = $this->getModel()->newQuery()
            ->where('staff_id', $condition->staffId)
            ->where('read', false);

        if ($condition->ids !== []) {
            $query->whereIn('id', $condition->ids);
        }

        return $query->update([
            'read' => true,
            'updated_at' => Carbon::now(),
            'updated_by' => $condition->staffId,
        ]);
    }

    #[\Override]
    public function existsForStaff(int $staffId, int $id): bool
    {
        return Model::query()
            ->where('staff_id', $staffId)
            ->where('id', $id)
            ->exists();
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function persist(Entity $entity): void
    {
        $this->save($entity);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getModel(): Model
    {
        return new Model;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getEntity(): Entity
    {
        return new Entity;
    }
}
