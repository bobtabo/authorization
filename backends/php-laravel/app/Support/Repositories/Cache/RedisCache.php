<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories\Cache;

use Cache;
use Illuminate\Cache\RedisStore;
use Illuminate\Support\Facades\Redis;

/**
 * Redisを操作するクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Cache
 */
class RedisCache
{
    /**
     * キャッシュします。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @param mixed $value キャッシュ値
     * @param int|null $second 有効期限（秒）
     * @return void
     */
    public static function put(CacheKey $cacheKey, mixed $value, ?int $second = null): void
    {
        if (empty($value)) {
            return;
        }

        if (!(Cache::getStore() instanceof RedisStore)) {
            Cache::put($cacheKey->tag[0] . '.' . $cacheKey->key, $value, $second);
            return;
        }

        Cache::setPrefix(config('authorization.app.cache_prefix'));
        Cache::tags($cacheKey->tag)->put($cacheKey->key, $value, $second);

        foreach ($cacheKey->tag as $tag) {
            Redis::sadd(static::tagSetKey($tag), $cacheKey->key);
        }
    }

    /**
     * キャッシュから取得します。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return mixed キャッシュ値
     */
    public static function get(CacheKey $cacheKey): mixed
    {
        if (Cache::getStore() instanceof RedisStore) {
            Cache::setPrefix(config('authorization.app.cache_prefix'));
            return Cache::tags($cacheKey->tag)->get($cacheKey->key);
        }

        return Cache::get($cacheKey->tag[0] . '.' . $cacheKey->key);
    }

    /**
     * キャッシュを削除します。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return void
     * @throws \Psr\SimpleCache\InvalidArgumentException キャッシュ例外
     */
    public static function delete(CacheKey $cacheKey): void
    {
        if (!(Cache::getStore() instanceof RedisStore)) {
            Cache::forget($cacheKey->tag[0] . '.' . $cacheKey->key);
            return;
        }

        Cache::setPrefix(config('authorization.app.cache_prefix'));
        Cache::tags($cacheKey->tag)->forget($cacheKey->key);

        foreach ($cacheKey->tag as $tag) {
            Redis::srem(static::tagSetKey($tag), $cacheKey->key);
        }
    }

    /**
     * キャッシュをクリアします。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return void
     */
    public static function flush(CacheKey $cacheKey): void
    {
        if (!(Cache::getStore() instanceof RedisStore)) {
            Cache::flush();
            return;
        }

        Cache::setPrefix(config('authorization.app.cache_prefix'));

        foreach ($cacheKey->tag as $tag) {
            $tagSetKey = static::tagSetKey($tag);
            $keys = Redis::smembers($tagSetKey);

            foreach ($keys as $key) {
                Cache::tags([$tag])->forget($key);
            }

            Redis::unlink($tagSetKey);
        }
    }

    /**
     * キャッシュをキーのみで取得します。
     *
     * @param string $key キャッシュキー
     * @return mixed キャッシュ値
     */
    public static function getByKey(string $key): mixed
    {
        if (Cache::getStore() instanceof RedisStore) {
            Cache::setPrefix(config('authorization.app.cache_prefix'));
        }

        return Cache::get($key);
    }

    /**
     * タグ管理セットの Redis キーを返します。
     *
     * @param string $tag タグ名
     * @return string Redis キー
     */
    private static function tagSetKey(string $tag): string
    {
        $prefix = config('authorization.app.cache_prefix');
        return empty($prefix) ? "tag_keys:{$tag}" : "{$prefix}:tag_keys:{$tag}";
    }
}
