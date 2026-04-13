<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Invitation\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * 招待 URL・トークンをまとめたスナップショット ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\ValueObjects
 *
 * @method bool isFound() 招待が見つかったかどうか
 * @method ?string getUrl() 招待 URL（完全）
 * @method ?string getDisplayUrl() 招待 URL（省略表示用）
 * @method ?string getToken() 招待トークン
 */
class InvitationVo extends AbstractValueObject
{
    use Getter;

    private bool $found = false;
    private ?string $url = null;
    private ?string $displayUrl = null;
    private ?string $token = null;
}
