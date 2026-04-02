<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Tests\Command;

use App\Support\Tests\CommandTesterTrait;
use Tests\TestCase;

/**
 * サンプルTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Command
 */
class ExampleTest extends TestCase
{
    use CommandTesterTrait;

    private $command;

    /**
     * {@inheritdoc}
     */
    public function setUp(): void
    {
        parent::setUp();

        $this->command = $this->getCommandTester(
            new \App\Console\Commands\ExampleCommand,
            'command:example'
        );
    }

    /**
     * A basic test example.
     *
     * @return void
     */
    public function testExample()
    {
        $this->assertTrue(true);
    }
}
