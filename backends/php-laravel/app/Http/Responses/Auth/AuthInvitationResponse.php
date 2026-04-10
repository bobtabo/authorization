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
 * 招待トークン検証Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Auth
 */
class AuthInvitationResponse extends AbstractResponse
{
    use Getter;

    private ?string $url = null;
    private ?string $displayUrl = null;
    private ?string $token = null;
}
