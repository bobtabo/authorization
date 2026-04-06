<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Invitation\Entities;

use App\Support\Entities\AbstractEntity;

/**
 * 招待Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\Entities
 */
class Invitation extends AbstractEntity
{
    public ?int $id = null;

    public ?string $token = null;

    /**
     * 招待用の完全 URL またはパス（API 応答用）。
     */
    public ?string $url = null;
}
