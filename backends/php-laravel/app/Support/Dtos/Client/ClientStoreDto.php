<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Dtos\Client;

use App\Support\Dtos\AbstractDto;

/**
 * クライアント登録の入力 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Dtos\Client
 */
class ClientStoreDto extends AbstractDto
{
    public ?string $name = null;

    public ?string $identifier = null;

    public ?string $post_code = null;

    public ?string $pref = null;

    public ?string $city = null;

    public ?string $address = null;

    public ?string $building = null;

    public ?string $tel = null;

    public ?string $email = null;

    public int $actorId = 0;
}
