<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Dtos;

use App\Support\Enums\SortType;

/**
 * ページングDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Dtos
 */
class PagerDto extends AbstractDto
{
    public int $offset = 0;
    public int $limit = 0;
    public string $sort = '';
    public SortType $sortType = SortType::NONE;

    /**
     * コンストラクタ
     */
    public function __construct()
    {
        parent::__construct();

        $this->limit = config('sii.app.list.limit.default');
    }

    /**
     * ページング設定します。
     *
     * @param int $page ページ番号
     * @return void
     */
    public function setPage(int $page): void
    {
        $this->offset = $this->limit * ($page - 1);
    }
}
