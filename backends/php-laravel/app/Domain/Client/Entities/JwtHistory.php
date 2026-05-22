<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\Entities;

use App\Support\Entities\AbstractEntity;
use Carbon\Carbon;

/**
 * Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain
 */
class JwtHistory extends AbstractEntity
{
    public ?int $id = null;
    public ?int $clientId = null;
    public ?string $memberId = null;
    public ?Carbon $issueAt = null;
    public ?string $jwt = null;
}
