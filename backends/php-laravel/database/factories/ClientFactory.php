<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use App\Infrastructure\Models\Client as Model;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * クライアントFactoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class ClientFactory extends Factory
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
            'name' => $this->faker->company(),
            'identifier' => $this->faker->unique()->slug(2),
            'post_code' => '100-0001',
            'pref' => '東京都',
            'city' => '千代田区',
            'address' => '千代田1-1-1',
            'building' => null,
            'tel' => '03-0000-0000',
            'email' => $this->faker->unique()->safeEmail(),
            'access_token' => bin2hex(random_bytes(16)),
            'private_key' => 'test_private_key',
            'public_key' => 'test_public_key',
            'fingerprint' => 'SHA256:testfingerprint',
            'status' => 1,
            'start_at' => now(),
            'stop_at' => null,
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
