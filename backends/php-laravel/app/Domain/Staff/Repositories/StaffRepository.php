<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Repositories;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff as Entity;
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
     * スタッフを取得します。
     *
     * @param StaffCondition $condition 検索条件
     * @return Entity|null エンティティ
     */
    public function findById(StaffCondition $condition): ?Entity;

    /**
     * スタッフを取得します。
     *
     * @param StaffCondition $condition 検索条件
     * @return Entity|null エンティティ
     */
    public function findByProvider(StaffCondition $condition): ?Entity;

    /**
     * スタッフを新規登録または更新して永続化します。
     *
     * {@see \App\Support\Repositories\AbstractEloquentRepository::save} とは別シグネチャのため persist とします。
     *
     * @param  Entity  $entity  永続化するエンティティ（id 未設定で新規）
     * @return Entity 保存後のエンティティ
     */
    public function persist(Entity $entity): Entity;

    /**
     * スタッフを論理削除します。
     *
     * @param  int  $id スタッフID
     * @param  int  $executorId  処理実行者ID
     * @return bool 対象が存在して削除できた場合 true
     */
    public function deleteById(int $id, int $executorId): bool;
}
