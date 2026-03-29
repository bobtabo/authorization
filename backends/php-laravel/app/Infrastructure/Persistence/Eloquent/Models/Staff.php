<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Models;

use App\Support\Models\AppMasterModel;
use Database\Factories\StaffFactory;

/**
 * スタッフModelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Models
 */
class Staff extends AppMasterModel
{
    protected $table = 'staffs';

    protected $guarded = [];

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return StaffFactory::new();
    }
}
