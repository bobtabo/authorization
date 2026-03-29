<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Enums;

/**
 * クライアントの状態を表す列挙型です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Enums
 */
enum ClientStatus: int
{
    case Inactive = 0;
    case Active = 1;
    case Suspended = 2;
    case Closed = 3;
}
