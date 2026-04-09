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
            'id' => '',
            'name' => '',
            'identifier' => '',
            'post_code' => '',
            'pref' => '',
            'city' => '',
            'address' => '',
            'building' => '',
            'tel' => '',
            'email' => '',
            'access_token' => '',
            'private_key' => '',
            'public_key' => '',
            'fingerprint' => '',
            'status' => '',
            'start_at' => '',
            'stop_at' => '',
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
