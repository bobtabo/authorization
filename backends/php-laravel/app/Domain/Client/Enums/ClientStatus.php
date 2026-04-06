<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\Enums;

use App\Support\Enums\Values;

/**
 * クライアントの状態を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Enums
 */
enum ClientStatus: int
{
    use Values;

    case Inactive = 1;
    case Active = 2;
    case Suspended = 3;
    case Closed = 4;
}
