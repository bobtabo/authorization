<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Infrastructure\Persistence\Eloquent\Models;

use App\Support\Models\AppMasterModel;
use Database\Factories\ClientFactory;

/**
 * クライアントModelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Persistence\Eloquent\Models
 */
class Client extends AppMasterModel
{
    protected $table = 'clients';

    protected $guarded = [];

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return ClientFactory::new();
    }
}
