<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Repositories;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff as Entity;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Infrastructure\Models\Staff as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Illuminate\Support\Collection;

/**
 * Eloquent によりスタッフを読み書きするRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class EloquentStaffEloquentRepository extends AbstractEloquentRepository implements StaffRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByCondition(StaffCondition $condition): Collection
    {
        $query = Model::withTrashed()->newQuery()
            ->select('staffs.*');

        if (!empty($condition->keyword)) {
            $keyword = str($condition->keyword)->trim()->replace(' ', '')->value();
            $query->where(function ($subQuery) use ($keyword) {
                $subQuery->whereLike('staffs.name', "%{$keyword}%")
                    ->orWhereLike('staffs.email', "%{$keyword}%");
            });
        }

        if (!empty($condition->roles)) {
            $query->whereIn('clients.role', $condition->roles);
        }

        if (!empty($condition->statuses)) {
            $query->whereIn('clients.status', $condition->statuses);
        }

        return $this->findByQuery($query);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findById(StaffCondition $condition): ?Entity
    {
        /** @var Entity $result */
        $result = $this->findByPk($condition->id);
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
    public function updateRole(int $id, StaffRole $role, int $executorId): bool
    {
        // TODO: Implement updateRole() method.
        return true;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function deleteById(int $id, int $executorId): bool
    {
        return $this->delete($id, $executorId);
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
