<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Dtos\Client;

use App\Support\Dtos\AbstractDto;

/**
 * クライアント削除の入力 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Dtos\Client
 */
class ClientDestroyDto extends AbstractDto
{
    public ?int $id = null;

    public int $actorId = 0;
}
