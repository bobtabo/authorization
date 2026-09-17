<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Persistence;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff as Entity;
use App\Domain\Staff\Enums\StaffStatus;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Infrastructure\Models\Staff as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use App\Support\Repositories\Traits\OptionBuilder;
use Illuminate\Contracts\Database\Eloquent\Builder;
use Illuminate\Support\Collection;

/**
 * スタッフRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence
 */
class EloquentStaffRepository extends AbstractEloquentRepository implements StaffRepository
{
    use OptionBuilder;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByCondition(StaffCondition $condition): Collection
    {
        $query = Model::withTrashed()->newQuery()
            ->select('staffs.*');

        $this->applyFilters($query, $condition);

        if ($condition->isPaging()) {
            $query = $this->addOption($query, $condition->option);
        }

        return $this->findByQuery($query);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function countByCondition(StaffCondition $condition): int
    {
        $query = Model::withTrashed()->newQuery()
            ->select('staffs.*');

        $this->applyFilters($query, $condition);

        return $query->count();
    }

    /**
     * 検索フィルタをクエリに適用します。
     *
     * @param  Builder  $query  クエリ
     * @param  StaffCondition  $condition  検索条件
     */
    private function applyFilters($query, StaffCondition $condition): void
    {
        if (!empty($condition->keyword)) {
            $keyword = str($condition->keyword)->trim()->replace(' ', '')->value();
            $query->where(function ($subQuery) use ($keyword) {
                $subQuery->whereLike('staffs.name', "%{$keyword}%")
                    ->orWhereLike('staffs.email', "%{$keyword}%");
            });
        }

        if (!empty($condition->roles)) {
            $query->whereIn('staffs.role', $condition->roles);
        }

        if (!empty($condition->statuses)) {
            // staffs テーブルに status カラムは無く、deleted_at の有無で有効/無効を判定する。
            $active = in_array(StaffStatus::Active->value, $condition->statuses, true);
            $inactive = in_array(StaffStatus::Inactive->value, $condition->statuses, true);
            if ($active && !$inactive) {
                $query->whereNull('staffs.deleted_at');
            } elseif ($inactive && !$active) {
                $query->whereNotNull('staffs.deleted_at');
            }
        }
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findById(StaffCondition $condition): ?Entity
    {
        $query = Model::withTrashed()->newQuery();

        /** @var Entity $result */
        $result = $this->findByPk($condition->id, $query);

        return $result;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByProvider(StaffCondition $condition): ?Entity
    {
        $query = Model::withTrashed()->newQuery()
            ->select('staffs.*')
            ->where('staffs.provider', $condition->provider)
            ->where('staffs.provider_id', $condition->providerId);

        /** @var Entity|null $result */
        $result = $this->findByQuery($query)->first();

        return $result;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function persist(Entity $entity): Entity
    {
        /** @var Entity $result */
        $result = $this->save($entity);

        return $result;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function deleteById(Entity $entity): bool
    {
        return $this->delete($entity->id, $entity->deletedBy);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function restoreById(Entity $entity): bool
    {
        $model = Model::withTrashed()->find($entity->id);
        if ($model === null || !$model->trashed()) {
            return false;
        }

        return $model->restore();
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findAllActive(): Collection
    {
        return $this->findByQuery(
            Model::query()->select('staffs.*')
        );
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
