<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Invitation\Entities\Invitation as Entity;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;
use App\Infrastructure\Models\Invitation as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Random\RandomException;

/**
 * 永続化未接続時に招待を仮返却するStubのRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class EloquentInvitationEloquentRepository extends AbstractEloquentRepository implements InvitationRepositoryInterface
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function getCurrent(): ?Entity
    {
        $model = Model::query()
            ->whereNull('deleted_at')
            ->orderByDesc('id')
            ->first();

        if ($model === null) {
            return null;
        }

        $entity = new Entity();
        $entity->assign([
            'token' => $model->token,
            'url' => '/auth/invitation/' . $model->token,
        ]);

        return $entity;
    }

    /**
     * {@inheritdoc}
     *
     * @throws RandomException 暗号論的乱数の生成に失敗した場合
     */
    #[\Override]
    public function issue(): Entity
    {
        $token = bin2hex(random_bytes(16));
        $invitation = new Entity;
        $invitation->assign([
            'token' => $token,
            'url' => '/auth/invitation/' . $token,
        ]);

        return $invitation;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByToken(string $token): ?Entity
    {
        $trimmed = trim($token);
        if ($trimmed === '') {
            return null;
        }

        $invitation = new Entity();
        $invitation->assign([
            'token' => $trimmed,
            'url' => '/auth/invitation/' . $trimmed,
        ]);

        return $invitation;
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
