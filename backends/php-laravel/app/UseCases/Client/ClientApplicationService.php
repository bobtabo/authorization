<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Client;

use App\Domain\Client\Entities\Client;
use App\Domain\Client\Repositories\ClientRepositoryInterface;

/**
 * クライアントの取得・一覧検索のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Client
 */
final class ClientApplicationService
{
    public function __construct(
        private readonly ClientRepositoryInterface $clients,
    ) {}

    public function get(int $id): ?Client
    {
        return $this->clients->findById($id);
    }

    /**
     * @return list<Client>
     */
    public function list(
        ?string $keyword = null,
        ?string $startFrom = null,
        ?string $startTo = null,
        array $statuses = [],
    ): array {
        return $this->clients->search($keyword, $startFrom, $startTo, $statuses);
    }
}
