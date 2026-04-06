<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Staff\Enums;

use App\Support\Enums\Values;

/**
 * プロバイダーを表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Enums
 */
enum Provider: int
{
    use Values;

    case Google = 1;
}
