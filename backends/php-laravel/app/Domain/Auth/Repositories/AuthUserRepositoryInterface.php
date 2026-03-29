<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Auth\Repositories;

use App\Domain\Auth\Entities\AuthUser;

/**
 * 認証ユーザーを永続化層から取得するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Auth\Repositories
 */
interface AuthUserRepositoryInterface
{
    public function findById(int $id): ?AuthUser;

    public function findByEmail(string $email): ?AuthUser;
}
