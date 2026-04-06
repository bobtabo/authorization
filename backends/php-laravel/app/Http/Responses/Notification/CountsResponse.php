<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Responses\Notification;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * 通知件数集計Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Notification
 */
class CountsResponse extends AbstractResponse
{
    use Getter;

    private int $unread = 0;
    private int $total = 0;
    private array $counts = [];
}
