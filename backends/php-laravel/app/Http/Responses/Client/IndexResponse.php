<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;
use Carbon\Carbon;

/**
 * クライアント一覧Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class IndexResponse extends AbstractResponse
{
    use Getter;

    /**
     * @var list<array<string, mixed>>
     */
    public array $items = [];

    /**
     * {@inheritdoc}
     *
     * 一覧 API は JSON 配列をルートに返すため、行ごとに TIMESTAMP を文字列へ正規化したリストを返します。
     *
     * @return list<array<string, mixed>>
     */
    #[\Override]
    public function attributes(): array
    {
        return array_map(
            static fn (array $row): array => self::normalizeRowTimestampsForJson($row),
            $this->items,
        );
    }

    /**
     * @param  array<string, mixed>  $row  ValueObject 行の attributes
     * @return array<string, mixed>
     */
    private static function normalizeRowTimestampsForJson(array $row): array
    {
        foreach (['start_at', 'stop_at', 'created_at', 'updated_at'] as $key) {
            if (! array_key_exists($key, $row)) {
                continue;
            }
            $v = $row[$key];
            if ($v instanceof Carbon) {
                $row[$key] = $v->toIso8601String();
            }
        }

        return $row;
    }
}
