<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support;

use Illuminate\Database\Eloquent\Model;

/**
 * エンティティ機能を提供するインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support
 */
interface Entity
{
    /**
     * 各フィールドにデータを設定します。
     *
     * @param array<string, mixed> $values データ
     * @param array<string, string> $convert 変換元 => 変換先の連想配列
     * @param array<string> $excludes 除外項目
     * @return mixed オブジェクト
     */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed;

    /**
     * 各フィールドにデータを設定します。
     *
     * @param Model $model モデル
     * @return Entity エンティティ
     */
    public function assignModel(Model $model): Entity;

    /**
     * 属性を取得します。
     *
     * @return array<string, mixed> 属性の配列
     */
    public function attributes(): array;

    /**
     * 作成者を設定します。
     *
     * @return Entity エンティティ
     */
    public function assignCreatedSystem(): Entity;

    /**
     * 作成者を設定します。
     *
       * @param int $executorId 処理実行者ID
     * @return Entity エンティティ
     */
    public function assignCreated(int $executorId): Entity;

    /**
     * 更新者を設定します。
     *
     * @return Entity エンティティ
     */
    public function assignUpdatedSystem(): Entity;

    /**
     * 更新者を設定します。
     *
     * @param int $executorId 処理実行者ID
     * @return Entity エンティティ
     */
    public function assignUpdated(int $executorId): Entity;

    /**
     * 削除者を設定します。
     *
     * @return Entity エンティティ
     */
    public function assignDeletedSystem(): Entity;

    /**
     * 削除者を設定します。
     *
     * @param int $executorId 処理実行者ID
     * @return Entity エンティティ
     */
    public function assignDeleted(int $executorId): Entity;
}
