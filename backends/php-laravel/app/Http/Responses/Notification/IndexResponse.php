<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Responses\Notification;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * 通知一覧Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Notification
 */
class IndexResponse extends AbstractResponse
{
    use Getter;

    private array $items = [];
    private ?string $next_cursor = null;
}
