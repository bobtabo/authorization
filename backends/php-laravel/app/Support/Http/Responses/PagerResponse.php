<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Responses;

use App\Support\Http\Responses\Traits\Pager;
use Illuminate\Support\Collection;

/**
 * ページャーResponseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http\Responses
 *
 * @method Collection getList()
 * @method string|null getKeyword()
 */
class PagerResponse extends AbstractResponse
{
    use Pager;

    protected Collection $list;
    protected ?string $keyword = null;
    protected mixed $pager = null;

    /**
     * @inheritdoc}
     */
    public function attributes(): array
    {
        $this->pager = $this->createPager($this->list->count());
        return parent::attributes();
    }
}
