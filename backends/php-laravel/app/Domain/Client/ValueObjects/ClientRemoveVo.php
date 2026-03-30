<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\ValueObjects;

use App\Support\Dtos\AbstractDto;

/**
 * クライアント削除の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 */
class ClientRemoveVo extends AbstractDto
{
    public bool $ok = false;

    public string $message = 'SUCCESS';
}
