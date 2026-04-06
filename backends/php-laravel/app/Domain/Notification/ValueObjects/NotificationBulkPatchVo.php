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
 * 通知一括更新の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\ValueObjects
 *
 * @method int getUpdated() 更新された通知の数を返します。
 * @method string getMessage() 結果メッセージを返します。
 */
class NotificationBulkPatchVo extends AbstractValueObject
{
    use Getter;

    private int $updated = 0;
    private string $message = 'SUCCESS';
}
