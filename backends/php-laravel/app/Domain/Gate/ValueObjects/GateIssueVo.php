<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Gate\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * JWT 発行結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Gate\ValueObjects
 *
 * @method string getMessage() 結果メッセージを返します。
 */
class GateIssueVo extends AbstractValueObject
{
    use Getter;

    private string $message = 'SUCCESS';
}
