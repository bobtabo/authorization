<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Staff\Repositories;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff;
use Illuminate\Support\Collection;

/**
 * スタッフを取得・条件検索・更新するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Repositories
 */
interface StaffRepository
{
    /**
     * スタッフリストを検索します。
     *
     * @param StaffCondition $condition 検索条件
     * @return Collection コレクション
     */
    public function findByCondition(StaffCondition $condition): Collection;

    /**
     * ID でスタッフを取得します。
     *
     * @param StaffCondition $condition 検索条件（id を設定すること）
     * @return Staff|null エンティティ
     */
    public function findById(StaffCondition $condition): ?Staff;

    /**
     * プロバイダー情報でスタッフを取得します。
     *
     * @param StaffCondition $condition 検索条件
     * @return Staff|null エンティティ
     */
    public function findByProvider(StaffCondition $condition): ?Staff;

    /**
     * スタッフを新規登録または更新して永続化します。
     *
     * @param Staff $entity 永続化するエンティティ（id 未設定で新規）
     * @return Staff 保存後のエンティティ
     */
    public function persist(Staff $entity): Staff;

    /**
     * スタッフを論理削除します。
     *
     * @param Staff $entity エンティティ
     * @return bool 対象が存在して削除できた場合 true
     */
    public function deleteById(Staff $entity): bool;

    /**
     * スタッフの論理削除を復元します。
     *
     * @param Staff $entity エンティティ
     * @return bool 対象が存在して復元できた場合 true
     */
    public function restoreById(Staff $entity): bool;

    /**
     * 有効なスタッフ全件を取得します（論理削除済み除外）。
     *
     * @return Collection スタッフエンティティのコレクション
     */
    public function findAllActive(): Collection;
}
