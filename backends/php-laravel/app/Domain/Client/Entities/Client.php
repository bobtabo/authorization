<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Entities;

use app\Domain\AbstractEntity;
use App\Domain\Client\Enums\ClientStatus;

/**
 * クライアント集約のルートを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Entities
 */
final readonly class Client extends AbstractEntity
{
    /**
     * @param  int  $id  クライアントID
     * @param  string  $name  名称
     * @param  string  $identifier  識別子
     * @param  ClientStatus  $status  状態
     */
    public function __construct(
        public int $id,
        public string $name,
        public string $identifier,
        public ClientStatus $status,
    ) {}
}
