<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories\Conditions;

use App\Support\Traits\Assign;
use App\Support\Traits\Attribute;

/**
 * 検索値を管理する基底クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Conditions
 */
class AbstractCondition
{
    use Assign, Attribute;

    public ?Option $option = null;
    public ?int $id = null;
    public array $ids = [];

    /**
     * ページング可能であるか確認します。
     *
     * @return bool ページング可能な場合 true を返します
     */
    public function isPaging(): bool
    {
        if (empty($this->option)) {
            return false;
        }

        return (!is_null($this->option->offset) && !empty($this->option->limit));
    }

    /**
     * 並び順に設定されているか確認します。
     *
     * @param string $column 対象カラム
     * @return bool 設定されている場合 true を返します
     */
    public function isOrderBy(string $column): bool
    {
        if (empty($this->option)) {
            return false;
        }

        return $this->option->isOrderColumn($column);
    }
}
