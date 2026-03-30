<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Staff;

use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Repositories\StaffRepositoryInterface;
use App\Support\Services\AbstractService;
use Illuminate\Database\QueryException;

/**
 * スタッフの取得・一覧検索のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Staff
 */
class StaffApplicationService extends AbstractService
{
    /**
     * @param  StaffRepositoryInterface  $staffRepository  スタッフRepository
     */
    public function __construct(
        private readonly StaffRepositoryInterface $staffRepository,
    ) {}

    /**
     * ID でスタッフを1件取得します。
     *
     * @param  int  $id  スタッフID
     * @return Staff|null 該当がなければ null
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function get(int $id): ?Staff
    {
        return $this->staffRepository->findById($id);
    }

    /**
     * 条件でスタッフ一覧を取得します。
     *
     * @param  string|null  $keyword  名前・メール向けキーワード（null または空は無条件）
     * @param  array<int, int>  $roles  権限コードの一覧（空は無条件）
     * @param  array<int, int>  $statuses  状態コードの一覧（空は無条件）
     * @return list<Staff> スタッフのリスト
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function list(
        ?string $keyword = null,
        array $roles = [],
        array $statuses = [],
    ): array {
        return $this->staffRepository->search($keyword, $roles, $statuses);
    }
}
