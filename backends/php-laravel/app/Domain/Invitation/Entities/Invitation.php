<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
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
}
