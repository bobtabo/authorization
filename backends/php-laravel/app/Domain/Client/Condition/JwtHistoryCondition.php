<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\Condition;

use App\Support\Repositories\Conditions\AbstractCondition;

/**
 * JWT履歴Conditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Condition
 */
class JwtHistoryCondition extends AbstractCondition
{
    public ?int $clientId = null;
}
