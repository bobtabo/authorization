<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;

/**
 * クライアント一覧の HTTP レスポンス用オブジェクトです（JSON ルートが配列になるよう attributes を調整します）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class ClientIndexResponse extends AbstractResponse
{
    /**
     * @var list<array<string, mixed>>
     */
    public array $items = [];

    /**
     * {@inheritdoc}
     *
     * 一覧 API は JSON 配列をルートに返すため、行のリストのみを返します。
     *
     * @return list<array<string, mixed>>
     */
    #[\Override]
    public function attributes(): array
    {
        return $this->items;
    }
}
