<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\JwtIssueHistory\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * JWT発行履歴DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\JwtIssueHistory\Dtos
 */
class JwtIssueHistoryDto extends AbstractDto
{
    public ?int $clientId = null;
}
