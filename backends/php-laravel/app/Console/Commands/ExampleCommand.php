<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Console\Commands;

use Illuminate\Console\Command;

/**
 * サンプルCommandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands
 */
class ExampleCommand extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'command:example';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'サンプルバッチ';

    /**
     * 実行します。
     *
     * @throws \Throwable 実行エラー時にスローされる例外です
     */
    public function handle()
    {
        //
    }
}
