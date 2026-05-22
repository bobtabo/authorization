<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Seeders;

use App\Support\Database\Seeder;

/**
 * JWT履歴Seederクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Seeders;
 */
class JwtHistorySeeder extends Seeder
{
    /**
     * {@inheritdoc}
     */
    protected ?string $target = 'jwt_histories';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function run(): void
    {
        parent::run();
    }
}
