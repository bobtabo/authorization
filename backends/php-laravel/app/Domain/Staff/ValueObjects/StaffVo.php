<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * スタッフValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\ValueObjects
 *
 * @method int|null getId()
 * @method string|null getName()
 * @method string|null getEmail()
 * @method string|null getAvatar()
 * @method int|null getRole()
 * @method int|null getStatus()
 */
class StaffVo extends AbstractValueObject
{
    use Getter;

    private ?int $id = null;
    private ?string $name = null;
    private ?string $email = null;
    private ?string $avatar = null;
    private ?int $role = null;
    private ?int $status = null;
}
