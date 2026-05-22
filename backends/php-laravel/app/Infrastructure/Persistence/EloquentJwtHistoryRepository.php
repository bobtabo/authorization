<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Persistence;

use App\Domain\Client\Condition\JwtHistoryCondition;
use App\Domain\Client\Entities\JwtHistory as Entity;
use App\Domain\Client\Repositories\JwtHistoryRepository;
use App\Infrastructure\Models\JwtHistory as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Illuminate\Support\Collection;

/**
 * JWT履歴Repositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence
 */
class EloquentJwtHistoryRepository extends AbstractEloquentRepository implements JwtHistoryRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByClientId(JwtHistoryCondition $condition): Collection
    {
        $query = Model::query()
            ->where('client_id', $condition->clientId)
            ->orderBy('issue_at', 'desc');

        return $this->findByQuery($query);
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
