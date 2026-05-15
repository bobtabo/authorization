<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * スマホアプリ向けクライアント情報ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 *
 * @method string|null getIdentifier()
 * @method string|null getName()
 * @method int|null getStatus()
 */
class ClientInfoVo extends AbstractValueObject
{
    use Getter;

    protected ?string $identifier = null;
    protected ?string $name = null;
    protected ?int $status = null;
}
