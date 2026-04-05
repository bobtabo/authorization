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
 * 通知トリガー受理Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Notification
 */
class IssueResponse extends AbstractResponse
{
    use Getter;

    private string $message = '';
    private mixed $received = null;
}
