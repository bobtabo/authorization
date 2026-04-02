<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Database\Factories;

use App\Infrastructure\Persistence\Eloquent\Models\Client as Model;
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
