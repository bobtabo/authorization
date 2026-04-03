<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Tests;

use Illuminate\Console\Command;
use Illuminate\Contracts\Console\Kernel;
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
        /** @var \Illuminate\Foundation\Console\Kernel $kernel */
        $kernel = $this->app->make(Kernel::class);
        $symfonyApp = $kernel->getArtisan();

        $command->setLaravel($this->app);

        $symfonyApp->add($command);

        $target = $symfonyApp->find($signature);

        return new CommandTester($target);
    }
}
