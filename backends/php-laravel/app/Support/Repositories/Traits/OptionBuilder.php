<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Repositories\Traits;

use App\Support\Repositories\Conditions\Option;
use Illuminate\Contracts\Database\Eloquent\Builder;

/**
 * オプション設定Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Traits
 */
trait OptionBuilder
{
    /**
     * オプションを設定します。
     *
     * @param Builder $query クエリー
     * @param Option|null $option オプション
     * @return Builder クエリー
     */
    public function addOption(Builder $query, ?Option $option): Builder
    {
        if (empty($option)) {
            return $query;
        }

        if (!is_null($option->limit)) {
            $query->limit($option->limit);

            if (!is_null($option->offset)) {
                $query->offset($option->offset);
            }
        }

        if (!empty($option->orderBy)) {
            $query->orderBy($option->orderBy);
        }

        if (!empty($option->orderByDesc)) {
            $query->orderByDesc($option->orderByDesc);
        }

        return $query;
    }
}
