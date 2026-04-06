<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Staff\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * スタッフ1件分の API 向けフィールドを保持する ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\ValueObjects
 *
 * @method bool isFound()
 * @method int|null getId()
 * @method string|null getName()
 * @method string|null getEmail()
 * @method int|null getRole()
 * @method int|null getStatus()
 */
class StaffResourceVo extends AbstractValueObject
{
    use Getter;

    private bool $found = false;
    private ?int $id = null;
    private ?string $name = null;
    private ?string $email = null;
    private ?int $role = null;
    private ?int $status = null;
}
