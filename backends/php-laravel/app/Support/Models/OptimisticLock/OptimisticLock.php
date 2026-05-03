<?php

/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */

declare(strict_types=1);

namespace Sii\Selloop\Core\Traits;

use App\Support\Exceptions\AppException;
use App\Support\Models\AppModel;
use Illuminate\Database\Eloquent\Casts\Attribute;

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
        $check = function (AppModel $model): void {
            $key = $model->getKeyName();
            $current = $model->withTrashed()->find($model->$key);
            if ($model->getOriginal('version') != $current?->version) {
                throw AppException::internal('optimistic lock');
            }
            $model->version++;
        };

        static::updating($check);
        static::deleting($check);
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
