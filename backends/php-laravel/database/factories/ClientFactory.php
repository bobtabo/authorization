<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Database\Factories;

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
    protected $model = Client::class;

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
