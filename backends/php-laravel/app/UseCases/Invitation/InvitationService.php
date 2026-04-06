<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Invitation;

use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;
use App\Domain\Invitation\ValueObjects\InvitationVo;
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
     * @param InvitationRepositoryInterface $invitations 招待Repository
     */
    public function __construct(
        private readonly InvitationRepositoryInterface $invitations,
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
        $vo = new InvitationVo();
        $entity = $this->invitations->getCurrent();
        if ($entity === null) {
            return $vo;
        }

        return $vo->assign([
            'found' => true,
            'url' => $entity->url,
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
        unset($dto);
        $entity = $this->invitations->issue();

        return (new InvitationVo())->assign([
            'found' => true,
            'url' => $entity->url,
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
        $vo = new InvitationVo();
        $token = $dto->token;
        if (!is_string($token) || $token === '') {
            return $vo;
        }

        $entity = $this->invitations->findByToken($token);
        if ($entity === null) {
            return $vo;
        }

        return $vo->assign([
            'found' => true,
            'url' => $entity->url,
            'token' => $entity->token,
        ]);
    }
}
