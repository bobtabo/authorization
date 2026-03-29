<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Auth;

use App\Domain\Auth\Entities\AuthUser;
use App\Domain\Auth\Repositories\AuthUserRepositoryInterface;

/**
 * 認証ユーザー参照のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Auth
 */
final class AuthApplicationService
{
    public function __construct(
        private readonly AuthUserRepositoryInterface $users,
    ) {}

    public function findUser(int $id): ?AuthUser
    {
        return $this->users->findById($id);
    }
}
