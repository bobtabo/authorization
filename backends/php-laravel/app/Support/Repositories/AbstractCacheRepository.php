<?php

/**
 *
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories;

use App\Support\Repositories\Cache\CacheKey;
use App\Support\Repositories\Cache\RedisCache;
use BackedEnum;

/**
 * 基底キャッシュRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories
 */
abstract class AbstractCacheRepository
{
    /**
     * キャッシュします。
     *
     * @param BackedEnum|string $tag キャッシュタグ
     * @param string $key キャッシュキー
     * @param mixed $value キャッシュ値
     * @param int|null $second 有効期限（秒）
     * @return void
     */
    public function put(BackedEnum|string $tag, string $key, mixed $value, ?int $second = null): void
    {
        $cacheTag = ($tag instanceof BackedEnum) ? $tag->value : $tag;
        $cacheKey = CacheKey::getCacheKey($cacheTag, $key);
        RedisCache::put($cacheKey, $value, $second);
    }

    /**
     * キャッシュから取得します。
     *
     * @param BackedEnum|string $tag キャッシュタグ
     * @param string $key キャッシュキー
     * @return mixed キャッシュ値
     */
    public function get(BackedEnum|string $tag, string $key): mixed
    {
        $cacheTag = ($tag instanceof BackedEnum) ? $tag->value : $tag;
        $cacheKey = CacheKey::getCacheKey($cacheTag, $key);
        return RedisCache::get($cacheKey);
    }

    /**
     * キャッシュを削除します。
     *
     * @param BackedEnum|string $tag キャッシュタグ
     * @param string $key キャッシュキー
     * @return void
     * @throws \Psr\SimpleCache\InvalidArgumentException キャッシュ例外
     */
    public function delete(BackedEnum|string $tag, string $key): void
    {
        $cacheTag = ($tag instanceof BackedEnum) ? $tag->value : $tag;
        $cacheKey = CacheKey::getCacheKey($cacheTag, $key);
        RedisCache::delete($cacheKey);
    }

    /**
     * キャッシュをクリアします。
     *
     * @param BackedEnum|string $tag キャッシュタグ
     * @return void
     */
    public function flush(BackedEnum|string $tag): void
    {
        $cacheTag = ($tag instanceof BackedEnum) ? $tag->value : $tag;
        $cacheKey = CacheKey::getCacheKey($cacheTag);
        RedisCache::flush($cacheKey);
    }
}
