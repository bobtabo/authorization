<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Account\Repositories;

use App\Domain\Account\Entities\Account;

/**
 * アカウントを取得・条件検索するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Account\Repositories
 */
interface AccountRepositoryInterface
{
    public function findById(int $id): ?Account;

    /**
     * @return list<Account>
     */
    public function search(
        ?string $keyword = null,
        array $roles = [],
        array $statuses = [],
    ): array;
}
