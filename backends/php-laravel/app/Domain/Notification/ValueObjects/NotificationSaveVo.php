<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Notification\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * 通知更新ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\ValueObjects
 */
class NotificationSaveVo extends AbstractValueObject
{
    use Getter;

    private bool $ok = false;
    private string $message = 'SUCCESS';
    private int $id = 0;
    private int $updated = 0;
}
