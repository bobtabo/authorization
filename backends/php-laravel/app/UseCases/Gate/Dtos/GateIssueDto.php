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
 * JWT発行DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate\Dtos
 */
class GateIssueDto extends AbstractDto
{
    public string $memberId = '';
    public string $accessToken = '';
}
