<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use App\Infrastructure\Models\Invitation as Model;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * 招待Factoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class InvitationFactory extends Factory
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
            'token' => '',
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
