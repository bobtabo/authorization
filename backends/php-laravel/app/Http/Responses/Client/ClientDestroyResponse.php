<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;

/**
 * クライアント削除の HTTP レスポンス用オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class ClientDestroyResponse extends AbstractResponse
{
    use Getter;

    public string $message = 'SUCCESS';
}
