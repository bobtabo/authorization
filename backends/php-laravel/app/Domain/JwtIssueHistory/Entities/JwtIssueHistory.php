<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\JwtIssueHistory\Entities;

use App\Support\Entities\AbstractEntity;
use Illuminate\Support\Carbon;

/**
 * Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain
 */
class JwtIssueHistory extends AbstractEntity
{
    public ?int $id = null;
    public ?int $clientId = null;
    public ?string $memberId = null;
    public ?Carbon $issueAt = null;
    public ?string $jwt = null;
}
