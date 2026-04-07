<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories\Conditions;

use App\Support\Enums\SortType;

/**
 * オプションクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Conditions
 */
class Option
{
    public ?int $offset = null;
    public ?int $limit = null;
    public ?string $orderBy = null;
    public ?string $orderByDesc = null;

    /**
     * コンストラクタ
     *
     * @param int|null $offset オフセット
     * @param int|null $limit リミット
     * @param string|null $sort ソート対象
     * @param SortType|null $sortType ソート種類
     */
    public function __construct(
        ?int $offset = null,
        ?int $limit = null,
        ?string $sort = null,
        ?SortType $sortType = SortType::NONE
    ) {
        $this->offset = $offset;
        $this->limit = $limit;
        $this->orderBy = ($sortType === SortType::ASC) ? $sort : null;
        $this->orderByDesc = ($sortType === SortType::DESC) ? $sort : null;

        if ($this->limit < 0) {
            $this->offset = null;
            $this->limit = null;
        }
    }

    /**
     * 並び順が設定されているか確認します。
     *
     * @return bool 設定されている場合 true を返します
     */
    public function hasOrderBy(): bool
    {
        return (!empty($this->orderBy) || !empty($this->orderByDesc));
    }

    /**
     * 対象カラムが並び順に設定されているか確認します。
     *
     * @param string $column 対象カラム
     * @return bool 設定されている場合 true を返します
     */
    public function isOrderColumn(string $column): bool
    {
        if (!$this->hasOrderBy()) {
            return false;
        }

        return ($this->orderBy == $column || $this->orderByDesc == $column);
    }
}
