<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Models;

use App\Support\Models\AppTransactionModel;
use Database\Factories\NotificationFactory;

/**
 * 通知Modelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Models
 */
class Notification extends AppTransactionModel
{
    /**
     * {@inheritdoc}
     */
    protected $casts = [
        'read' => 'boolean',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'deleted_at' => 'datetime',
    ];

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return NotificationFactory::new();
    }
}
