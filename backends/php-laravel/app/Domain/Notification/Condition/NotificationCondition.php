<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Notification\Condition;

use App\Support\Repositories\Conditions\AbstractCondition;

/**
 * 通知Conditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Condition
 */
class NotificationCondition extends AbstractCondition
{
    public ?int $staffId = null;
    public ?int $messageType = null;
    public ?string $title = null;
    public ?string $message = null;
    public bool $read = false;
    public bool $countUnread = false;
    public ?string $cursor;
    public int $limit = 1;
    public array $ids = [];
    public bool $all = false;
}
