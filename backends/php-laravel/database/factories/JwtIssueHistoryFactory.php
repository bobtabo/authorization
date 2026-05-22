<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * JWT履歴Factoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class JwtIssueHistoryFactory extends Factory
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
            'client_id' => '',
            'member_id' => '',
            'issue_at' => '',
            'jwt' => '',
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
