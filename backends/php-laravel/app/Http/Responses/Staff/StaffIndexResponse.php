<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Staff;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Http\Responses\Traits\Pager;
use App\Support\Traits\Getter;

/**
 * スタッフ一覧Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Staff
 */
class StaffIndexResponse extends AbstractResponse
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
        $pager = $this->createPager(count($this->items));

        return [
            'data' => $this->items,
            'pager' => $pager,
        ];
    }
}
