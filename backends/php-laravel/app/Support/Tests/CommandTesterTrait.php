<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Tests;

use Illuminate\Console\Command;
use Symfony\Component\Console\Tester\CommandTester;

/**
 * コマンドテストTraitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Tests
 */
trait CommandTesterTrait
{
    /**
     * コマンドテストオブジェクトを取得します。
     *
     * @param Command $command コマンドオブジェクト
     * @param string $signature コマンド名
     * @return CommandTester コマンドテストオブジェクト
     */
    protected function getCommandTester(Command $command, string $signature)
    {
        \Illuminate\Support\Facades\Artisan::starting(function ($artisan) use ($command) {
            $artisan->add($command);
        });

        $kernel = $this->app->make(\Illuminate\Contracts\Console\Kernel::class);

        $this->artisan('list'); // 魔法の一行：これで内部の Artisan を強制起動
        $symfonyApp = $this->app->make('artisan');

        $target = $symfonyApp->find($signature);
        return new CommandTester($target);
    }
}
