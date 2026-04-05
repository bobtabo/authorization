<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Responses\Staff;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * スタッフ削除Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Staff
 */
class StaffDestroyResponse extends AbstractResponse
{
    use Getter;

    private string $message = '';
    private ?int $id = null;
}
