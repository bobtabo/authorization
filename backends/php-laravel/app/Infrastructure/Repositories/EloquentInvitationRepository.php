<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Invitation\Condition\InvitationCondition;
use App\Domain\Invitation\Entities\Invitation as Entity;
use App\Domain\Invitation\Repositories\InvitationRepository;
use App\Infrastructure\Models\Invitation as Model;
use App\Support\Enums\SortType;
use App\Support\Repositories\AbstractEloquentRepository;
use App\Support\Repositories\Conditions\Option;

/**
 * 招待Repositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class EloquentInvitationRepository extends AbstractEloquentRepository implements InvitationRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function getCurrent(): ?Entity
    {
        $option = new Option(null, null, 'id', SortType::DESC);
        $list = $this->all($option);

        if ($list->isEmpty()) {
            return null;
        }

        /** @var Entity $result */
        $result = $list->first();
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
    public function findByToken(InvitationCondition $condition): ?Entity
    {
        $list = $this->findByMap(['token' => $condition->token]);
        if ($list->isEmpty()) {
            return null;
        }

        /** @var Entity $result */
        $result = $list->first();
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
