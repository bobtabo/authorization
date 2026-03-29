<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Repositories;

use App\Domain\Client\Entities\Client;

/**
 * クライアントを取得・条件検索するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Repositories
 */
interface ClientRepositoryInterface
{
    public function findById(int $id): ?Client;

    /**
     * @return list<Client>
     */
    public function search(
        ?string $keyword = null,
        ?string $startFrom = null,
        ?string $startTo = null,
        array $statuses = [],
    ): array;
}
