<?php

/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Models\OptimisticLock;

use Illuminate\Database\Eloquent\Casts\Attribute;

/**
 * 楽観的ロックTraitです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Support\Models\OptimisticLock
 */
trait OptimisticLock
{
    /**
     * 排他オブサーバを起動します。
     *
     * @return void
     */
    protected static function bootOptimisticLock()
    {
        static::observe(OptimisticLockObserver::class);
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
