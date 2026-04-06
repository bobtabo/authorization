<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Models;

use App\Support\Models\AppMasterModel;
use Database\Factories\ClientFactory;

/**
 * クライアントModelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Models
 */
class Client extends AppMasterModel
{
    /**
     * {@inheritdoc}
     */
    protected $casts = [
        'start_at' => 'datetime',
        'stop_at' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'deleted_at' => 'datetime',
    ];

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return ClientFactory::new();
    }
}
