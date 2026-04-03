<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Staff;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Mappers\StaffApiMapper;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Domain\Staff\ValueObjects\StaffListVo;
use App\Domain\Staff\ValueObjects\StaffMutationVo;
use App\Domain\Staff\ValueObjects\StaffRemoveVo;
use App\Domain\Staff\ValueObjects\StaffResourceVo;
use App\Support\Services\AbstractService;
use App\UseCases\Staff\Dtos\StaffDto;
use Illuminate\Database\QueryException;

/**
 * スタッフの取得・一覧・更新・削除のユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Staff
 */
class StaffService extends AbstractService
{
    /**
     * @param  StaffRepository  $staffRepository  スタッフRepository
     */
    public function __construct(
        private readonly StaffRepository $staffRepository,
    ) {}

    /**
     * ID でスタッフを1件取得します。
     *
     * @param  StaffDto  $dto  スタッフDTO
     * @return StaffResourceVo スタッフリソースValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function find(StaffDto $dto): StaffResourceVo
    {
        $vo = new StaffResourceVo;
        if ($dto->id === null) {
            return $vo;
        }

        $condition = new StaffCondition;
        $condition->id = $dto->id;

        $entity = $this->staffRepository->findById($condition);
        if ($entity === null) {
            return $vo;
        }

        $vo->assign(array_merge(['found' => true], StaffApiMapper::toListItem($entity)));

        return $vo;
    }

    /**
     * 条件でスタッフ一覧を取得します。
     *
     * @param  StaffDto  $dto  スタッフDTO
     * @return StaffListVo スタッフ一覧ValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function index(StaffDto $dto): StaffListVo
    {
        $condition = new StaffCondition;
        $condition->keyword = $dto->keyword;
        $condition->roles = $dto->roles;
        $condition->statuses = $dto->statuses;

        $list = $this->staffRepository->findByCondition($condition);

        $vo = new StaffListVo;
        $vo->assignStaff($list);

        return $vo;
    }

    /**
     * 権限を更新します。
     *
     * @param  StaffDto  $dto  スタッフDTO
     * @return StaffMutationVo スタッフ権限更新ValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function updateRole(StaffDto $dto): StaffMutationVo
    {
        $vo = new StaffMutationVo;
        if ($dto->id === null || $dto->role === null) {
            return $vo;
        }

        $role = StaffRole::tryFrom($dto->role);
        if ($role === null) {
            return $vo;
        }

        $ok = $this->staffRepository->updateRole($dto->id, $role, $dto->executorId);
        $vo->assign(['ok' => $ok, 'id' => $dto->id]);

        return $vo;
    }

    /**
     * スタッフを論理削除します。
     *
     * @param  StaffDto  $dto  スタッフDTO
     * @return StaffRemoveVo スタッフ削除ValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function destroy(StaffDto $dto): StaffRemoveVo
    {
        $vo = new StaffRemoveVo;
        if ($dto->id === null) {
            return $vo;
        }

        $ok = $this->staffRepository->deleteById($dto->id, $dto->executorId);
        $vo->assign(['ok' => $ok, 'id' => $dto->id]);

        return $vo;
    }
}
