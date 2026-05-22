<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use App\Infrastructure\Models\JwtHistory as Model;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * JWT履歴Factoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class JwtHistoryFactory extends Factory
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
            'client_id' => 1,
            'member_id' => $this->faker->uuid(),
            'issue_at' => now(),
            'jwt' => $this->faker->sha256(),
            'created_at' => now(),
            'created_by' => 0,
            'updated_at' => now(),
            'updated_by' => 0,
            'deleted_at' => null,
            'deleted_by' => null,
            'version' => 1,
        ];
    }
}
