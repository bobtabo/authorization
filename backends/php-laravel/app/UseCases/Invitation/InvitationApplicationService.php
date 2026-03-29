<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Invitation;

use App\Domain\Invitation\Entities\Invitation;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;

/**
 * 招待の取得・発行のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Invitation
 */
final class InvitationApplicationService
{
    public function __construct(
        private readonly InvitationRepositoryInterface $invitations,
    ) {}

    public function current(): ?Invitation
    {
        return $this->invitations->getCurrent();
    }

    public function issue(): Invitation
    {
        return $this->invitations->issue();
    }
}
