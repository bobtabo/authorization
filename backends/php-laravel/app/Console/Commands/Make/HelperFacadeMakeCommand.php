<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Sii\Selloop\Core\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * ヘルパーリファサード生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Sii\Selloop\Core\Console\Commands\Make
 */
class HelperFacadeMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:helper-facade';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'ヘルパーファサード生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'HelperFacade';

    /**
     * {@inheritdoc}
     */
    protected function getStub()
    {
        return __DIR__ . '/Stubs/helper-facade.stub';
    }

    /**
     * {@inheritdoc}
     */
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\Helpers\Facades';
    }

    /**
     * {@inheritdoc}
     */
    protected function getPath($name)
    {
        return parent::getPath($name . 'Facade');
    }
}
