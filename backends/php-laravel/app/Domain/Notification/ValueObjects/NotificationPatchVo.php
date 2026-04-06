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
 * 単一通知更新の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\ValueObjects
 */
class NotificationPatchVo extends AbstractValueObject
{
    use Getter;

    private bool $ok = false;
    private string $message = 'SUCCESS';
    private string $id = '';
}
