<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Notification\Entities;

/**
 * 通知1件の状態を表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Entities
 */
final readonly class Notification
{
    public function __construct(
        public string $id,
        public string $title,
        public ?\DateTimeImmutable $readAt,
    ) {}
}
