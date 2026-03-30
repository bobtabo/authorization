<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Invitation;

use App\Domain\Invitation\Entities\Invitation;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;
use App\Support\Services\AbstractService;
use Random\RandomException;

/**
 * 招待の取得・発行のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Invitation
 */
class InvitationApplicationService extends AbstractService
{
    /**
     * @param  InvitationRepositoryInterface  $invitations  招待Repository
     */
    public function __construct(
        private readonly InvitationRepositoryInterface $invitations,
    ) {}

    /**
     * 現在の招待情報を取得します。
     *
     * @return Invitation|null 未設定時は null
     */
    public function current(): ?Invitation
    {
        return $this->invitations->getCurrent();
    }

    /**
     * 新しい招待を発行します。
     *
     * @return Invitation 発行された招待
     * @throws RandomException 乱数生成に失敗した場合（永続化実装に依存）
     */
    public function issue(): Invitation
    {
        return $this->invitations->issue();
    }
}
