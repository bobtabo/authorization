<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Auth\Entities\AuthUser;
use App\Domain\Auth\Repositories\AuthUserRepositoryInterface;
use App\Models\User;

/**
 * Laravel User をドメインの認証ユーザーへ変換して返すRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
final class EloquentAuthUserRepository implements AuthUserRepositoryInterface
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findById(int $id): ?AuthUser
    {
        $row = User::query()->find($id);

        if ($row === null) {
            return null;
        }

        return new AuthUser(
            id: (int) $row->getKey(),
            name: (string) $row->name,
            email: (string) $row->email,
        );
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByEmail(string $email): ?AuthUser
    {
        $row = User::query()->where('email', $email)->first();

        if ($row === null) {
            return null;
        }

        return new AuthUser(
            id: (int) $row->getKey(),
            name: (string) $row->name,
            email: (string) $row->email,
        );
    }
}
