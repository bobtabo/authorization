<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Gate;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * JWT発行Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Gate
 */
class GateIssueResponse extends AbstractResponse
{
    use Getter;

    private string $message = '';
}
