<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Database\Factories;

use App\Domain\Invitation\Entities\Invitation;
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
    protected $model = Invitation::class;

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
