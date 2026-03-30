<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Auth\Entities\Auth;
use App\Domain\Auth\Repositories\AuthUserRepositoryInterface;
use App\Models\User;
use app\Support\Repositories\AbstractRepository;
use Illuminate\Database\QueryException;

/**
 * Laravel User をドメインの認証ユーザーへ変換して返すRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
class EloquentAuthUserRepository extends AbstractRepository implements AuthUserRepositoryInterface
{
    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function findById(int $id): ?Auth
    {
        $row = User::query()->find($id);

        if ($row === null) {
            return null;
        }

        return new Auth(
            id: (int) $row->getKey(),
            name: (string) $row->name,
            email: (string) $row->email,
        );
    }

    /**
     * {@inheritdoc}
     *
     * @throws QueryException クエリ実行に失敗した場合
     */
    #[\Override]
    public function findByEmail(string $email): ?Auth
    {
        $row = User::query()->where('email', $email)->first();

        if ($row === null) {
            return null;
        }

        return new Auth(
            id: (int) $row->getKey(),
            name: (string) $row->name,
            email: (string) $row->email,
        );
    }
}
