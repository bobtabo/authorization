<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Notification\Entities;

use App\Support\Entities\AbstractEntity;

/**
 * 通知Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Notification\Entities
 */
class Notification extends AbstractEntity
{
    public ?int $id = null;
    public ?string $token = null;
}
