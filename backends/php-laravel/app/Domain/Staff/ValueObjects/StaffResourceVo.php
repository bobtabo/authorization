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
 * スタッフ1件分の API 向けフィールドを保持する ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\ValueObjects
 */
class StaffResourceVo extends AbstractValueObject
{
    use Getter;

    public bool $found = false;

    public ?int $id = null;

    public ?string $name = null;

    public ?string $email = null;

    public ?int $role = null;

    public ?int $status = null;
}
