<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Repositories;

use App\Domain\Staff\Entities\Staff;

/**
 * スタッフを取得・条件検索するRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Repositories
 */
interface StaffRepositoryInterface
{
    /**
     * ID でスタッフを1件取得します。
     *
     * @param  int  $id  スタッフID
     * @return Staff|null 該当がなければ null
     */
    public function findById(int $id): ?Staff;

    /**
     * 条件でスタッフを検索します。
     *
     * @param  string|null  $keyword  名前・メール向けキーワード（null または空は無条件）
     * @param  array<int, int>  $roles  権限コードの一覧（空は無条件）
     * @param  array<int, int>  $statuses  状態コードの一覧（空は無条件）
     * @return list<Staff> スタッフのリスト
     */
    public function search(
        ?string $keyword = null,
        array $roles = [],
        array $statuses = [],
    ): array;
}
