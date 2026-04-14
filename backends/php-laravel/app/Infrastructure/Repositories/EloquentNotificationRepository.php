<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Notification\Entities\Notification as Entity;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Infrastructure\Models\Notification as Model;
use Carbon\Carbon;

/**
 * Eloquent により通知を読み書きするRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class EloquentNotificationRepository implements NotificationRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function listPage(int $staffId, ?string $cursor, int $limit): array
    {
        $query = Model::query()
            ->where('staff_id', $staffId)
            ->orderBy('created_at', 'desc')
            ->orderBy('id', 'desc');

        if ($cursor !== null) {
            $decoded = base64_decode($cursor, true);
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

        $rows = $query->limit($limit + 1)->get();
        $hasNext = $rows->count() > $limit;
        $items = $hasNext ? $rows->slice(0, $limit) : $rows;

        $nextCursor = null;
        if ($hasNext) {
            $last = $items->last();
            $nextCursor = base64_encode($last->created_at->format('Y-m-d H:i:s') . ',' . $last->id);
        }

        $entities = $items->map(fn($model) => (new Entity())->assign($model->toArray()))->values()->all();

        return [
            'items' => $entities,
            'next_cursor' => $nextCursor,
        ];
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function counts(int $staffId): array
    {
        $total = Model::query()->where('staff_id', $staffId)->count();
        $unread = Model::query()->where('staff_id', $staffId)->where('read', false)->count();

        return [
            'unread' => $unread,
            'total' => $total,
            'counts' => [],
        ];
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
    public function store(int $staffId, int $messageType, string $title, string $message, int $executorId, ?string $url = null): void
    {
        $now = Carbon::now();
        $model = new Model();
        $model->fill([
            'staff_id' => $staffId,
            'message_type' => $messageType,
            'title' => $title,
            'message' => $message,
            'url' => $url,
            'read' => false,
            'created_by' => $executorId,
            'updated_by' => $executorId,
            'version' => 1,
        ]);
        $model->created_at = $now;
        $model->updated_at = $now;
        $model->save();
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
}
