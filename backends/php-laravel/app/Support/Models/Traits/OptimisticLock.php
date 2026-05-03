<?php

/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Models\Traits;

use App\Support\Exceptions\AppException;
use App\Support\Models\AppModel;
use Illuminate\Database\Eloquent\Casts\Attribute;

/**
 * 楽観的ロックTraitです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Support\Models\Traits
 */
trait OptimisticLock
{
    /**
     * 楽観的ロックのイベントリスナーを登録します。
     *
     * @return void
     */
    protected static function bootOptimisticLock(): void
    {
        static::updating(static::exclusion(...));
        static::deleting(static::exclusion(...));
    }

    /**
     * 排他を確認します。
     *
     * @param AppModel $model
     * @return void
     */
    protected static function exclusion(AppModel $model): void
    {
        $current = $model->withTrashed()->find($model->getKey());
        if ($model->getOriginal('version') != $current?->version) {
            throw AppException::internal('optimistic lock');
        }
        $model->version++;
    }

    /**
     * バージョンを設定します。
     */
    protected function version(): Attribute
    {
        return Attribute::make(
            set: fn (int|null $value) => ['version' => $value ?: 1],
        );
    }
}
