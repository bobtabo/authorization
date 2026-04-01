<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace app\Domain\Staff\ValueObjects;

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
 * @method int|null getRole()
 * @method int|null getStatus()
 */
class StaffVo extends AbstractValueObject
{
    use Getter;

    public ?int $id = null;
    public ?string $name = null;
    public ?string $email = null;
    public ?int $role = null;
    public ?int $status = null;
}
