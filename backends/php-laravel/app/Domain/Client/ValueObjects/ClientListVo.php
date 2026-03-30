<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\ValueObjects;

use App\Domain\Client\Entities\Client;
use App\Domain\Client\Mappers\ClientApiMapper;
use App\Support\Dtos\AbstractDto;

/**
 * クライアント一覧の結果 ValueObject です（API 行の配列を保持します）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 */
class ClientListVo extends AbstractDto
{
    /**
     * @var list<array<string, mixed>>
     */
    public array $items = [];

    /**
     * Entity コレクションを API 行へ変換して items に設定します。
     *
     * @param  list<Client>  $clients
     * @return $this
     */
    public function assignClients(array $clients): self
    {
        $this->items = array_map(
            static fn (Client $c): array => ClientApiMapper::toResponseArray($c),
            $clients,
        );

        return $this;
    }
}
