<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Entities;

use App\Domain\Staff\Enums\Provider;
use App\Domain\Staff\Enums\StaffRole;
use App\Domain\Staff\Enums\StaffStatus;
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
    public ?Provider $provider = null;
    public ?string $providerId = null;
    public ?string $avatar = null;
    public ?StaffRole $role = null;
    public ?StaffStatus $status = null;
    public ?Carbon $lastLoginAt = null;
}
