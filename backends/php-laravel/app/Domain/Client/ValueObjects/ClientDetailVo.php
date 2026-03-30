<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\ValueObjects;

use App\Support\Dtos\AbstractDto;

/**
 * クライアント詳細の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 */
class ClientDetailVo extends AbstractDto
{
    public bool $found = false;

    /**
     * API レスポンス向けに整形済みのクライアント（スネークケースキー）。
     *
     * @var array<string, mixed>|null
     */
    public ?array $client = null;

    /**
     * {@inheritdoc}
     *
     * ClientShowResponse::assign に渡すため、クライアント属性のみを返します。
     *
     * @return array<string, mixed>
     */
    #[\Override]
    public function attributes(): array
    {
        if (! $this->found || $this->client === null) {
            return [];
        }

        return $this->client;
    }
}
