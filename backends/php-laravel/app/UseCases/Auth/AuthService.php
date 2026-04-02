<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Auth;

use App\Domain\Staff\Condition\StaffCondition;
use App\Domain\Staff\Enums\Provider;
use App\Domain\Staff\Repositories\StaffRepository;
use app\Domain\Staff\ValueObjects\StaffVo;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Auth\Dtos\SocialDto;

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
    ) {}

    /**
     * 認証処理を行います。
     *
     * @param SocialDto $dto ソーシャルDTO
     * @return StaffVo スタッフValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException
     */
    public function login(SocialDto $dto): StaffVo
    {
        /** @var StaffCondition $condition */
        $condition = SimpleMapper::mapSpecific($dto, StaffCondition::class, [
            'provider' => Provider::Google,
            'provider_id' => $dto->id,
        ]);

        $entity = $this->staffRepository->findByProvider($condition);

        $vo =new StaffVo();
        if (empty($entity)) {
            $entity->assignCreated($dto->executorId);
            $saved = $this->staffRepository->persist($entity);
            $vo->assign($saved->attributes());
        } else {
            $vo->assign($entity->attributes());
        }

        return $vo;
    }
}
