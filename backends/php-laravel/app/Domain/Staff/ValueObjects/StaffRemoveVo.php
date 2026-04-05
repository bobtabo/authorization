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
 * スタッフ削除の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\ValueObjects
 *
 * @method bool isOk()
 * @method string getMessage()
 * @method int|null getId()
 */
class StaffRemoveVo extends AbstractValueObject
{
    use Getter;

    private bool $ok = false;
    private string $message = 'SUCCESS';
    private ?int $id = null;
}
