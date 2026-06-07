<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Http\Responses\Traits\Pager;
use App\Support\Traits\Getter;
use Carbon\Carbon;

/**
 * JWT履歴一覧Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class JwtHistoryResponse extends AbstractResponse
{
    use Getter;
    use Pager;

    /**
     * @var list<array<string, mixed>>
     */
    public array $items = [];

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        $data = array_map(
            static fn (array $row): array => self::normalizeRow($row),
            $this->items,
        );

        $pager = $this->createPager(count($data));

        return [
            'data'  => $data,
            'pager' => $pager,
        ];
    }

    /**
     * @param array<string, mixed> $row
     * @return array<string, mixed>
     */
    private static function normalizeRow(array $row): array
    {
        $result = [];
        foreach (['id', 'member_id', 'issue_at', 'jwt'] as $key) {
            $v = $row[$key] ?? null;
            $result[$key] = $v instanceof Carbon ? $v->format('Y-m-d H:i:s') : $v;
        }
        return $result;
    }
}
