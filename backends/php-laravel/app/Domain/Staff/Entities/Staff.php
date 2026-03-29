<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Entities;

use App\Domain\Staff\Enums\StaffRole;
use App\Support\Entities\AbstractEntity;
use Carbon\Carbon;

/**
 * スタッフEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Entities
 */
class Staff extends AbstractEntity
{
    public ?int $id = null;
    public ?string $name = null;
    public ?string $email = null;
    public ?int $provider = null;
    public ?string $providerId = null;
    public ?string $avater = null;
    public ?StaffRole $role = null;
    public ?Carbon $lastLoginAt = null;
}
