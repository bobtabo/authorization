<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Auth;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * 自分自身のプロフィールResponseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Auth
 */
class AuthMeResponse extends AbstractResponse
{
    use Getter;

    private ?int $staffId = null;
    private ?string $name = null;
    private ?string $avatar = null;
    private ?int $role = null;
}
