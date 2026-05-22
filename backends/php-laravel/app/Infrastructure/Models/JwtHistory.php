<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Models;

use App\Support\Models\AppTransactionModel;
use Database\Factories\JwtHistoryFactory;

/**
 * JWT履歴Modelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Models
 */
class JwtHistory extends AppTransactionModel
{
    /**
     * {@inheritdoc}
     */
    protected $table = 'jwt_histories';

    /**
     * {@inheritdoc}
     */
    protected $casts = [
        'issue_at' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'deleted_at' => 'datetime',
    ];

    /**
     * @return \Illuminate\Database\Eloquent\Relations\BelongsTo
     */
    public function client()
    {
        return $this->belongsTo(Client::class);
    }

    /**
     * {@inheritdoc}
     */
    protected static function newFactory()
    {
        return JwtHistoryFactory::new();
    }
}
