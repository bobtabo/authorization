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
     * 招待用の完全 URL（API 応答・コピー用）。
     */
    public ?string $url = null;

    /**
     * 表示用の省略 URL（長いトークン部分を ... で省略）。
     */
    public ?string $displayUrl = null;
}
