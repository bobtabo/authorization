<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * クライアントQRコードResponseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class QrResponse extends AbstractResponse
{
    use Getter;

    private ?string $identifier = null;
    private ?string $deeplinkUrl = null;
}
