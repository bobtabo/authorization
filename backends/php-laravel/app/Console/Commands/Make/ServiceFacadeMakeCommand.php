<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Sii\Selloop\Core\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * サービスファサード生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Sii\Selloop\Core\Console\Commands\Make
 */
class ServiceFacadeMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:service-facade';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'サービスファサード生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'ServiceFacade';

    /**
     * {@inheritdoc}
     */
    protected function getStub()
    {
        return __DIR__ . '/Stubs/service-facade.stub';
    }

    /**
     * {@inheritdoc}
     */
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\Domains\Services\Facades';
    }

    /**
     * {@inheritdoc}
     */
    protected function getPath($name)
    {
        return parent::getPath($name . 'Facade');
    }
}
