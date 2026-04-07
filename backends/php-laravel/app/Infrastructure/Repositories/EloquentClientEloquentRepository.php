<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client as Entity;
use App\Domain\Client\Repositories\ClientRepository;
use App\Infrastructure\Models\Client as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Illuminate\Support\Collection;

/**
 * Eloquent によりクライアントを永続化するRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class EloquentClientEloquentRepository extends AbstractEloquentRepository implements ClientRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByCondition(ClientCondition $condition): Collection
    {
        $query = Model::withTrashed()->newQuery()
            ->select('clients.*');

        if (!empty($condition->keyword)) {
            $keyword = str($condition->keyword)->trim()->replace(' ', '')->value();
            $query->whereLike('clients.name', "%{$keyword}%");
        }

        if (!empty($condition->startFrom) && !empty($condition->startTo)) {
            $query->whereBetween('matters.start_at', [ $condition->startFrom, $condition->startTo ]);
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
    public function findById(ClientCondition $condition): ?Entity
    {
        /** @var Entity $result */
        $result = $this->findByPk($condition->id);
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
