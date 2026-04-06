<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Models;

use App\Support\Models\AppMasterModel;
use Database\Factories\InvitationFactory;

/**
 * 招待Modelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Models
 */
class Invitation extends AppMasterModel
{
    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return InvitationFactory::new();
    }
}
