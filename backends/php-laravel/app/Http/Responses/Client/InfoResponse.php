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
 * スマホアプリ向けクライアント情報Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class InfoResponse extends AbstractResponse
{
    use Getter;

    private ?string $identifier = null;
    private ?string $name = null;
    private ?int $status = null;
}
