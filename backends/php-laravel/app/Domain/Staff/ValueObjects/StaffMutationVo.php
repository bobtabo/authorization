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
 * スタッフ権限更新の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\ValueObjects
 */
class StaffMutationVo extends AbstractValueObject
{
    use Getter;

    public bool $ok = false;

    public string $message = 'SUCCESS';

    public ?int $id = null;
}
