<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Repositories;

use App\Domain\Invitation\Entities\Invitation;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;

/**
 * 永続化未接続時に招待を仮返却するStubのRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Repositories
 */
final class StubInvitationRepository implements InvitationRepositoryInterface
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function getCurrent(): ?Invitation
    {
        return null;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function issue(): Invitation
    {
        $token = bin2hex(random_bytes(16));

        return new Invitation(
            token: $token,
            url: '/auth/invitation/'.$token,
        );
    }
}
