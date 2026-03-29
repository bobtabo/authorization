<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Entities;

use app\Domain\AbstractEntity;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Enums\StaffStatus;

/**
 * スタッフ集約のルートを表すEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Entities
 */
final readonly class Staff extends AbstractEntity
{
    /**
     * @param  int  $id  スタッフID
     * @param  string  $name  表示名
     * @param  string  $email  メールアドレス
     * @param  StaffStatus  $status  状態
     * @param  StaffRole  $role  権限
     */
    public function __construct(
        public int $id,
        public string $name,
        public string $email,
        public StaffStatus $status,
        public StaffRole $role,
    ) {}
}
