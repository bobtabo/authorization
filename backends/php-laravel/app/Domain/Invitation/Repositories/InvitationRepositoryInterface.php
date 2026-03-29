<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Invitation\Repositories;

use App\Domain\Invitation\Entities\Invitation;

/**
 * 招待の現在値取得と発行を担うRepositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\Repositories
 */
interface InvitationRepositoryInterface
{
    public function getCurrent(): ?Invitation;

    public function issue(): Invitation;
}
