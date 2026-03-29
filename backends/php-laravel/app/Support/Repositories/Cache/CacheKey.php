<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Repositories\Cache;

use Sii\Selloop\Core\Models\AppModel;

/**
 * キャッシュキーを管理するクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Cache
 */
class CacheKey
{
    public array $tag;
    public string $key;
    public bool $cached = false;

    /**
     * キャッシュキーを取得します。
     *
     * @param string $tag タグ
     * @param string $key キー
     * @param bool $cached キャッシュ利用しない場合 false を設定します
     * @return CacheKey キャッシュキー情報
     */
    public static function getCacheKey(string $tag, string $key = '', bool $cached = true): CacheKey
    {
        $result = new CacheKey();
        $result->tag = [ $tag ];
        $result->key = $key;
        $result->cached = $cached;
        return $result;
    }

    /**
     * キャッシュキーを取得します。
     *
     * @param AppModel $model モデル
     * @param array<int, mixed> $values データ配列
     * @return CacheKey キャッシュキー情報
     */
    public static function getCacheKeyByModel(AppModel $model, array $values = []): CacheKey
    {
        $tag = $model->getTable();
        $key = $tag . (!empty($values) ? '.' . implode('.', $values) : '');
        $cached = $model->cached;
        return static::getCacheKey($tag, $key, $cached);
    }
}
