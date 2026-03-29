<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Account;

use App\Domain\Account\Entities\Account;
use App\Domain\Account\Repositories\AccountRepositoryInterface;

/**
 * アカウントの取得・一覧検索のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Account
 */
final class AccountApplicationService
{
    public function __construct(
        private readonly AccountRepositoryInterface $accounts,
    ) {}

    public function get(int $id): ?Account
    {
        return $this->accounts->findById($id);
    }

    /**
     * @return list<Account>
     */
    public function list(
        ?string $keyword = null,
        array $roles = [],
        array $statuses = [],
    ): array {
        return $this->accounts->search($keyword, $roles, $statuses);
    }
}
