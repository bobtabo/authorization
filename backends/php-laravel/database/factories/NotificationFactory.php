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
            'staff_id' => 1,
            'message_type' => 1,
            'title' => $this->faker->sentence(),
            'message' => $this->faker->paragraph(),
            'read' => false,
            'created_at' => now(),
            'created_by' => 1,
            'updated_at' => now(),
            'updated_by' => 1,
            'deleted_at' => null,
            'deleted_by' => null,
            'version' => 1,
        ];
    }
}
