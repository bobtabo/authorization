<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Invitation\Entities;

use app\Domain\AbstractEntity;

/**
 * 招待トークンと招待URLを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\Entities
 */
final readonly class Invitation extends AbstractEntity
{
    /**
     * @param  string  $token  招待トークン
     * @param  string  $url  招待URL
     */
    public function __construct(
        public string $token,
        public string $url,
    ) {}
}
