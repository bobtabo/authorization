<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Database\Factories;

use App\Infrastructure\Persistence\Eloquent\Models\Staff as Model;
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
     * モデル生成用のデフォルト属性を返します。
     *
     * {@inheritdoc}
     *
     * @return array<string, mixed> 属性の連想配列
     */
    #[\Override]
    public function definition(): array
    {
        return [
            //
        ];
    }
}
