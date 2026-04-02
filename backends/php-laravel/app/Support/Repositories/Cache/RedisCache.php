<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Repositories\Cache;

use Cache;
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

        Cache::setPrefix(config('authorization.app.cache_prefix'));
        Cache::tags($cacheKey->tag)->put($cacheKey->key, $value, $second);
    }

    /**
     * キャッシュから取得します。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return mixed キャッシュ値
     */
    public static function get(CacheKey $cacheKey): mixed
    {
        Cache::setPrefix(config('authorization.app.cache_prefix'));
        return Cache::tags($cacheKey->tag)->get($cacheKey->key);
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
        Cache::setPrefix(config('authorization.app.cache_prefix'));
        Cache::tags($cacheKey->tag)->delete($cacheKey->key);
    }

    /**
     * キャッシュをクリアします。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return void
     */
    public static function flush(CacheKey $cacheKey): void
    {
        Cache::setPrefix(config('authorization.app.cache_prefix'));
        foreach ($cacheKey->tag as $key) {
            Cache::deleteMultiple(static::keys($key));
        }
        Cache::tags($cacheKey->tag)->flush();
    }

    /**
     * キャッシュをキーのみで取得します。
     *
     * @param string $key キャッシュキー
     * @return mixed キャッシュ値
     */
    public static function getByKey(string $key): mixed
    {
        Cache::setPrefix(config('authorization.app.cache_prefix'));
        return Cache::get($key);
    }

    /**
     * キャッシュキーをワイルドカードで取得します。
     *
     * @param string $key キャッシュキー
     * @return array キャッシュキー配列
     */
    private static function keys(string $key): array
    {
        $prefix = 'selloop_' . config('authorization.app.cache_prefix') . ':';
        $keys = Redis::keys('*' . $key . '*');
        if (empty($keys)) {
            return [];
        }

        $result = [];
        foreach ($keys as $keyName) {
            $result[] = str($keyName)->replace($prefix, '')->value();
        }

        return $result;
    }
}
