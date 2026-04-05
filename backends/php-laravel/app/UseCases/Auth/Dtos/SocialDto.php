<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Auth\Dtos;

use App\Domain\Staff\Enums\Provider;
use App\Support\Dtos\AbstractDto;

/**
 * ソーシャルDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Auth\Dtos
 */
class SocialDto extends AbstractDto
{
    public ?Provider $provider = null;
    public ?string $providerId = null;
    public ?string $nickname = null;
    public ?string $name = null;
    public ?string $email = null;
    public ?string $avatar = null;
}
