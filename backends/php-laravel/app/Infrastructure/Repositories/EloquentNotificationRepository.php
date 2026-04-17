<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Notification\Condition\NotificationCondition;
use App\Domain\Notification\Entities\Notification as Entity;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Infrastructure\Models\Notification as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Carbon\Carbon;
use Illuminate\Support\Collection;

/**
 * Eloquent により通知を読み書きするRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
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
                                ->where('id', '<', (int)$cursorId);
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
    public function bulkMarkRead(int $staffId, array $ids, bool $all): int
    {
        $query = Model::query()
            ->where('staff_id', $staffId)
            ->where('read', false);

        if (!$all && $ids !== []) {
            $query->whereIn('id', $ids);
        }

        return $query->update([
            'read' => true,
            'updated_at' => Carbon::now(),
        ]);
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
    public function patch(int $id, array $attributes): bool
    {
        $model = Model::query()->find($id);
        if ($model === null) {
            return false;
        }

        $allowed = ['read'];
        $filtered = array_intersect_key($attributes, array_flip($allowed));
        if (empty($filtered)) {
            return false;
        }

        $filtered['updated_at'] = Carbon::now();
        $model->fill($filtered)->save();

        return true;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getModel(): Model
    {
        return new Model();
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getEntity(): Entity
    {
        return new Entity();
    }
}
