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
 * スタッフ権限を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Enums
 */
enum StaffRole: int
{
    use Values;

    case Administrator = 1;
    case Member = 2;
}
