<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Models;

use App\Support\Models\AppMasterModel;
use Database\Factories\StaffFactory;

/**
 * スタッフModelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Models
 */
class Staff extends AppMasterModel
{
    /**
     * {@inheritdoc}
     */
    protected $casts = [
        'last_login_at' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'deleted_at' => 'datetime',
    ];

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return StaffFactory::new();
    }
}
