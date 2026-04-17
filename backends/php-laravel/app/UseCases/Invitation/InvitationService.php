<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Invitation;

use App\Domain\Invitation\Condition\InvitationCondition;
use App\Domain\Invitation\Repositories\InvitationRepository;
use App\Domain\Invitation\ValueObjects\InvitationVo;
use App\Support\Exceptions\AppException;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Invitation\Dtos\InvitationDto;
use Random\RandomException;

/**
 * 招待の取得・発行のユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Invitation
 */
class InvitationService extends AbstractService
{
    /**
     * @param InvitationRepository $repository 招待Repository
     */
    public function __construct(
        private readonly InvitationRepository $repository,
    ) {
    }

    /**
     * 現在の招待情報を取得します。
     *
     * @param InvitationDto $dto 招待DTO
     * @return InvitationVo 招待ValueObject
     */
    public function current(InvitationDto $dto): InvitationVo
    {
        unset($dto);
        $entity = $this->repository->getCurrent();
        if ($entity === null) {
            throw AppException::notFound('invitation_not_found');
        }

        return (new InvitationVo())->assign([
            'found' => true,
            'url' => $entity->url,
            'displayUrl' => $entity->displayUrl,
            'token' => $entity->token,
        ]);
    }

    /**
     * 新しい招待を発行します。
     *
     * @param InvitationDto $dto 招待DTO
     * @return InvitationVo 招待ValueObject
     * @throws RandomException 乱数生成に失敗した場合（永続化実装に依存）
     */
    public function issue(InvitationDto $dto): InvitationVo
    {
        $entity = $this->repository->issue();

        return new InvitationVo()->assign([
            'found' => true,
            'url' => $entity->url,
            'displayUrl' => $entity->displayUrl,
            'token' => $entity->token,
        ]);
    }

    /**
     * 招待トークンから招待情報を解決します。
     *
     * @param InvitationDto $dto 招待DTO
     * @return InvitationVo 招待ValueObject
     */
    public function findByToken(InvitationDto $dto): InvitationVo
    {
        $token = $dto->token;
        if (!is_string($token) || $token === '') {
            throw AppException::badRequest('invitation_invalid');
        }

        $condition = SimpleMapper::map($dto, InvitationCondition::class);
        $entity = $this->repository->findByToken($condition);
        if ($entity === null) {
            throw AppException::badRequest('invitation_invalid');
        }

        return (new InvitationVo())->assign([
            'found' => true,
            'url' => $entity->url,
            'displayUrl' => $entity->displayUrl,
            'token' => $entity->token,
        ]);
    }
}
