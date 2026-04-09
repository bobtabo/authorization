<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Staff\Repositories;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff as Entity;
use App\Domain\Staff\Enums\StaffRole;
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
     * @param  StaffCondition  $condition  検索条件
     * @return Collection コレクション
     */
    public function findByCondition(StaffCondition $condition): Collection;

    /**
     * ID でスタッフを取得します。
     *
     * @param  StaffCondition  $condition  検索条件（id を設定すること）
     * @return Entity|null エンティティ
     */
    public function findById(StaffCondition $condition): ?Entity;

    /**
     * プロバイダー情報でスタッフを取得します。
     *
     * @param  StaffCondition  $condition  検索条件
     * @return Entity|null エンティティ
     */
    public function findByProvider(StaffCondition $condition): ?Entity;

    /**
     * スタッフを新規登録または更新して永続化します。
     *
     * @param  Entity  $entity  永続化するエンティティ（id 未設定で新規）
     * @return Entity 保存後のエンティティ
     */
    public function persist(Entity $entity): Entity;

    /**
     * スタッフの権限を更新します。
     *
     * @param  int  $id  スタッフID
     * @param  StaffRole  $role  更新する権限
     * @param  int  $executorId  処理実行者ID
     * @return bool 対象が存在して更新できた場合 true
     */
    public function updateRole(int $id, StaffRole $role, int $executorId): bool;

    /**
     * スタッフを論理削除します。
     *
     * @param  int  $id  スタッフID
     * @param  int  $executorId  処理実行者ID
     * @return bool 対象が存在して削除できた場合 true
     */
    public function deleteById(int $id, int $executorId): bool;

    /**
     * スタッフの論理削除を復元します。
     *
     * @param  int  $id  スタッフID
     * @return bool 対象が存在して復元できた場合 true
     */
    public function restoreById(int $id): bool;

    /**
     * 有効なスタッフ全件を取得します（論理削除済み除外）。
     *
     * @return Collection スタッフエンティティのコレクション
     */
    public function findAllActive(): Collection;
}
