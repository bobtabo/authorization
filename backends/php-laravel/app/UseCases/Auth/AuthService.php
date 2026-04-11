<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Auth;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Domain\Staff\ValueObjects\StaffVo;
use App\Support\Exceptions\AppException;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Auth\Dtos\AuthUserDto;
use App\UseCases\Auth\Dtos\SocialDto;
use Carbon\Carbon;

/**
 * 認証Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Auth
 */
class AuthService extends AbstractService
{
    public function __construct(
        private readonly StaffRepository $staffRepository,
    ) {
    }

    /**
     * ID でスタッフ（ログインユーザー）を取得します。
     *
     * @param AuthUserDto $dto 認証ユーザーDTO
     * @return StaffVo スタッフValueObject（found = false のとき未存在）
     */
    public function findUser(AuthUserDto $dto): StaffVo
    {
        $condition = new StaffCondition();
        $condition->id = $dto->id;

        $entity = $this->staffRepository->findById($condition);
        if (empty($entity)) {
            throw AppException::noFound('user_not_found');
        }

        return (new StaffVo())->assign($entity->attributes());
    }

    /**
     * ソーシャル認証でログインします（未登録の場合は新規作成します）。
     *
     * @param SocialDto $dto ソーシャルDTO
     * @return StaffVo スタッフValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException
     */
    public function login(SocialDto $dto): StaffVo
    {
        /** @var StaffCondition $condition */
        $condition = SimpleMapper::map($dto, StaffCondition::class);
        $entity = $this->staffRepository->findByProvider($condition);

        $vo = new StaffVo();
        if (empty($entity)) {
            $newEntity = new Staff();
            $newEntity->assign($dto->attributes());
            $newEntity->role = StaffRole::Member;
            $newEntity->lastLoginAt = Carbon::now();
            $newEntity->assignCreated(0);
            $saved = $this->staffRepository->persist($newEntity);
        } else {
            $entity->avatar = $dto->avatar;
            $entity->lastLoginAt = Carbon::now();
            $entity->assignUpdated($entity->id);
            $saved = $this->staffRepository->persist($entity);
        }
        $vo->assign($saved->attributes());

        return $vo;
    }
}
