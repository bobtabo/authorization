<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\ValueObjects;

use App\Support\Dtos\AbstractDto;

/**
 * クライアント登録・更新の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 */
class ClientMutationVo extends AbstractDto
{
    public bool $ok = true;

    public string $message = 'SUCCESS';

    /**
     * @var array<string, mixed>
     */
    public array $client = [];
}
