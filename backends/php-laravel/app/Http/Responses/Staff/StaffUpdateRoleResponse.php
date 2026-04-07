<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Staff;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * スタッフ権限更新Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Staff
 */
class StaffUpdateRoleResponse extends AbstractResponse
{
    use Getter;

    private string $message = '';
    private ?int $id = null;
}
