<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use App\Infrastructure\Models\Staff as Model;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * スタッフFactoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class StaffFactory extends Factory
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
            'name' => $this->faker->name(),
            'email' => $this->faker->unique()->safeEmail(),
            'provider' => 1,
            'provider_id' => $this->faker->numerify('##########'),
            'avatar' => null,
            'role' => 1,
            'last_login_at' => now(),
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
