<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Account\Entities;

/**
 * アカウント集約のルートを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Account\Entities
 */
final readonly class Account
{
    public function __construct(
        public int $id,
        public string $name,
        public string $email,
        public int $activeValue,
        public int $roleValue,
    ) {}
}
