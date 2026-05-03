<?php

/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */

declare(strict_types=1);

namespace Sii\Selloop\Core\Traits;

use Illuminate\Database\Eloquent\Casts\Attribute;
use Sii\Selloop\Core\Observers\OptimisticLockObserver;

/**
 * 楽観的ロックTraitです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package Sii\Selloop\Core\Traits
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
        self::observe(OptimisticLockObserver::class);
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
