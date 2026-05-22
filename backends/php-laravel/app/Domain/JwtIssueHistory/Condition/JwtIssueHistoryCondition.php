<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\JwtIssueHistory\Condition;

use App\Support\Repositories\Conditions\AbstractCondition;

/**
 * JWT発行履歴Conditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\JwtIssueHistory\Condition
 */
class JwtIssueHistoryCondition extends AbstractCondition
{
    public ?int $clientId = null;
}
