<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Staff;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Mappers\StaffApiMapper;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Domain\Staff\ValueObjects\StaffListVo;
use App\Domain\Staff\ValueObjects\StaffMutationVo;
use App\Domain\Staff\ValueObjects\StaffRemoveVo;
use App\Domain\Staff\ValueObjects\StaffResourceVo;
use App\Support\Exceptions\AppException;
use App\Support\Mappers\SimpleMapper;
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
     * @param StaffRepository $repository スタッフRepository
     */
    public function __construct(
        private readonly StaffRepository $repository,
    ) {
    }

    /**
     * ID でスタッフを1件取得します。
     *
     * @param StaffDto $dto スタッフDTO
     * @return StaffResourceVo スタッフリソースValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function find(StaffDto $dto): StaffResourceVo
    {
        $vo = new StaffResourceVo();
        if ($dto->id === null) {
            return $vo;
        }

        $condition = new StaffCondition();
        $condition->id = $dto->id;

        $entity = $this->repository->findById($condition);
        if ($entity === null) {
            return $vo;
        }

        $vo->assign(array_merge(['found' => true], StaffApiMapper::toListItem($entity)));

        return $vo;
    }

    /**
     * 条件でスタッフ一覧を取得します。
     *
     * @param StaffDto $dto スタッフDTO
     * @return StaffListVo スタッフ一覧ValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function index(StaffDto $dto): StaffListVo
    {
        $condition = new StaffCondition();
        $condition->keyword = $dto->keyword;
        $condition->roles = $dto->roles;
        $condition->statuses = $dto->statuses;

        $list = $this->repository->findByCondition($condition);

        $vo = new StaffListVo();
        $vo->assignStaff($list);

        return $vo;
    }

    /**
     * 権限を更新します。
     *
     * @param StaffDto $dto スタッフDTO
     * @return StaffMutationVo スタッフ権限更新ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function updateRole(StaffDto $dto): StaffMutationVo
    {
        if ($dto->role === null) {
            throw AppException::badRequest('role_invalid');
        }

        $condition = SimpleMapper::map($dto, StaffCondition::class);
        $entity = $this->repository->findById($condition);

        if ($entity === null) {
            throw AppException::notFound('staff_not_found');
        }

        $entity->role = $dto->role;
        $entity->assignUpdated($dto->executorId);
        $saved = $this->repository->persist($entity);

        return new StaffMutationVo()->assign([
            'ok' => true,
            'id' => $saved->id
        ]);
    }

    /**
     * スタッフを論理削除します。
     *
     * @param StaffDto $dto スタッフDTO
     * @return StaffRemoveVo スタッフ削除ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function destroy(StaffDto $dto): StaffRemoveVo
    {
        /** @var Staff $entity */
        $entity = SimpleMapper::map($dto, Staff::class);
        $entity->assignDeleted($dto->executorId);
        $result = $this->repository->deleteById($entity);
        if (!$result) {
            throw AppException::notFound('staff_not_found');
        }

        return new StaffRemoveVo()->assign([
            'ok' => true,
            'id' => $dto->id
        ]);
    }

    /**
     * スタッフの論理削除を復元します。
     *
     * @param StaffDto $dto スタッフDTO
     * @return StaffRemoveVo スタッフ復元ValueObject
     * @throws QueryException 永続化層のクエリに失敗した場合
     */
    public function restore(StaffDto $dto): StaffRemoveVo
    {
        /** @var Staff $entity */
        $entity = SimpleMapper::map($dto, Staff::class);
        $result = $this->repository->restoreById($entity);
        if (!$result) {
            throw AppException::notFound('staff_not_found');
        }

        return (new StaffRemoveVo())->assign(['ok' => true, 'id' => $dto->id]);
    }
}
