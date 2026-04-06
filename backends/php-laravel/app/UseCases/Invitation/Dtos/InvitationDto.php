<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Invitation\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * 招待 API 用の入力 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Invitation\Dtos
 */
class InvitationDto extends AbstractDto
{
    public ?string $token = null;
}
