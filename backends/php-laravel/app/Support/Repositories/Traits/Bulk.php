<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories\Traits;

use App\Support\Repositories\Cache\CacheKey;
use App\Support\Repositories\Cache\RedisCache;
use Batch;
use DB;

/**
 * バルク操作Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Traits
 */
trait Bulk
{
    /**
     * 一括登録します。
     *
     * @param array $list 二次元配列の登録データ
     * @return void
     */
    public function insertBatch(array $list): void
    {
        $model = $this->getModel();
        $table = $model->getTable();
        DB::table($table)->insert($list);

        $cache = CacheKey::getCacheKeyByModel($model);
        RedisCache::flush($cache);
    }

    /**
     * 一括更新します。
     *
     * @param array $list 二次元配列の更新データ
     * @return void
     */
    public function updateBatch(array $list): void
    {
        $model = $this->getModel();
        Batch::update($model, $list, 'id');

        $cache = CacheKey::getCacheKeyByModel($model);
        RedisCache::flush($cache);
    }

    /**
     * 一括物理削除します。
     *
     * @param array $ids 削除ID配列
     * @return void
     */
    public function forceDeleteBatch(array $ids = []): void
    {
        if (empty($ids)) {
            return;
        }

        $model = $this->getModel();
        $model->newQuery()->whereIn('id', $ids)->forceDelete();

        $cache = CacheKey::getCacheKeyByModel($model);
        RedisCache::flush($cache);
    }
}
