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
 * スマホアプリ連携用QRコードValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 *
 * @method string|null getIdentifier()
 * @method string|null getDeeplinkUrl()
 */
class ClientQrVo extends AbstractValueObject
{
    use Getter;

    protected ?string $identifier = null;
    protected ?string $deeplinkUrl = null;
}
