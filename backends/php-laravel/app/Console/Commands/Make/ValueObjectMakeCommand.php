<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Sii\Selloop\Core\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * データオブジェクト生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Sii\Selloop\Core\Console\Commands\Make
 */
class ValueObjectMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:value-object';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'データオブジェクト生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'ValueObject';

    /**
     * {@inheritdoc}
     */
    protected function getStub()
    {
        return __DIR__ . '/Stubs/value-object.stub';
    }

    /**
     * {@inheritdoc}
     */
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\Domains\ValueObjects';
    }
}
