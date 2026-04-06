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
 * JWT検証Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Gate
 */
class GateVerifyResponse extends AbstractResponse
{
    use Getter;

    private ?string $iss = null;
    private ?string $sub = null;
    private ?string $aud = null;
    private int $exp = 0;
    private int $iat = 0;
    private int $nbf = 0;
    private ?string $jti = null;
}
