<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Auth\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * 認証ユーザーDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Auth\Dtos
 */
class AuthUserDto extends AbstractDto
{
    public ?int $id = null;
}
