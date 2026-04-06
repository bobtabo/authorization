<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Gate\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * JWT 発行リクエスト用 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate\Dtos
 */
class GateIssueDto extends AbstractDto
{
    public string $memberId = '';
}
