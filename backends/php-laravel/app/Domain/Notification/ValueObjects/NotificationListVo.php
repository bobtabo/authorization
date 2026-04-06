<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Notification\ValueObjects;

use App\Domain\Notification\Entities\Notification;
use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * 通知一覧ページの結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\ValueObjects
 */
class NotificationListVo extends AbstractValueObject
{
    use Getter;

    private array $items = [];
    private ?string $next_cursor = null;
}
