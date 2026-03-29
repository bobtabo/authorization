<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Invitation\Entities;

/**
 * 招待トークンと招待URLを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\Entities
 */
final readonly class Invitation
{
    public function __construct(
        public string $token,
        public string $url,
    ) {}
}
