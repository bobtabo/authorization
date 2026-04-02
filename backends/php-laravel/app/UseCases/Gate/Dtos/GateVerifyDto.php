<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Gate\Dtos;

use App\Support\Dtos\AbstractDto;

/**
 * JWT 検証リクエスト用 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate\Dtos
 */
class GateVerifyDto extends AbstractDto
{
    public string $identifier = '';

    public string $token = '';
}
