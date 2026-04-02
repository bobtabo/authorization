<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Notification\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * 通知件数集計の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\ValueObjects
 *
 * @method int getUnread() 未読通知の件数を返します。
 * @method int getTotal() 全通知の件数を返します。
 * @method array getCounts() タイプ別の通知件数を返します。
 */
class NotificationCountsVo extends AbstractValueObject
{
    use Getter;

    private int $unread = 0;
    private int $total = 0;
    private array $counts = [];
}
