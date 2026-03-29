<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Entities;

/**
 * クライアント集約のルートを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Entities
 */
final readonly class Client
{
    public function __construct(
        public int $id,
        public string $name,
        public string $identifier,
        public int $status,
    ) {}
}
