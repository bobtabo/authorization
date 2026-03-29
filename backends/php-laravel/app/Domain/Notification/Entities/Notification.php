<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Notification\Entities;

use app\Domain\AbstractEntity;

/**
 * 通知1件の状態を表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Entities
 */
final readonly class Notification extends AbstractEntity
{
    /**
     * @param  string  $id  通知ID
     * @param  string  $title  タイトル
     * @param  \DateTimeImmutable|null  $readAt  既読日時（未読は null）
     */
    public function __construct(
        public string $id,
        public string $title,
        public ?\DateTimeImmutable $readAt,
    ) {}
}
