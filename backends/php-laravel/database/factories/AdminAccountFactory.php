<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Database\Factories;

use App\Infrastructure\Persistence\Eloquent\Models\AdminAccountModel;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * 管理アカウントFactoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class AdminAccountFactory extends Factory
{
    /**
     * @var string モデル
     */
    protected $model = AdminAccountModel::class;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function definition(): array
    {
        return [
            //
        ];
    }
}
