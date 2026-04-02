<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Repositories\Cache;

/**
 * Redisを操作するモデル用クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Cache
 */
class RedisModelCache
{
    /**
     * @var bool|null クエリーキャッシュ使用する場合は true を設定します
     */
    private static ?bool $queryCache = null;

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
        if (!static::isCached($cacheKey)) {
            return;
        }
        RedisCache::put($cacheKey, $value, $second);
    }

    /**
     * キャッシュから取得します。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return mixed キャッシュ値
     */
    public static function get(CacheKey $cacheKey): mixed
    {
        if (!static::isCached($cacheKey)) {
            return null;
        }
        return RedisCache::get($cacheKey);
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
        if (!static::isCached($cacheKey)) {
            return;
        }
        RedisCache::delete($cacheKey);
    }

    /**
     * キャッシュをクリアします。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return void
     */
    public static function flush(CacheKey $cacheKey): void
    {
        if (!static::isCached($cacheKey)) {
            return;
        }
        RedisCache::flush($cacheKey);
    }

    /**
     * キャッシュ設定するか確認します。
     *
     * @param CacheKey $cacheKey キャッシュキー情報
     * @return bool キャッシュ
     */
    private static function isCached(CacheKey $cacheKey): bool
    {
        if (is_null(static::$queryCache)) {
            static::$queryCache = config('sii.app.query_cache');
        }

        if (static::$queryCache) {
            return $cacheKey->cached;
        }

        return false;
    }
}
