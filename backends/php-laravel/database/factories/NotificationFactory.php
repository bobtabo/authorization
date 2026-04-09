<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use App\Infrastructure\Models\Notification as Model;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * 通知Factoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class NotificationFactory extends Factory
{
    /**
     * @var string モデル
     */
    protected $model = Model::class;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function definition(): array
    {
        return [
            'id' => '',
            'staff_id' => '',
            'message_type' => '',
            'title' => '',
            'message' => '',
            'read' => '',
            'created_at' => '',
            'created_by' => '',
            'updated_at' => '',
            'updated_by' => '',
            'deleted_at' => '',
            'deleted_by' => '',
            'version' => '',
        ];
    }
}
