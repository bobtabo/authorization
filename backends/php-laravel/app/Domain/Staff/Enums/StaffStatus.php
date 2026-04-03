<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Staff\Enums;

use App\Support\Enums\Values;

/**
 * スタッフの状態を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\Enums
 */
enum StaffStatus: int
{
    use Values;

    case Inactive = 0;
    case Active = 1;
}
