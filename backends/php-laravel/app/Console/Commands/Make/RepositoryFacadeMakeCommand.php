<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * RepositoryFacade生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands\Make
 */
class RepositoryFacadeMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:repository-facade';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'リポジトリファサード生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'RepositoryFacade';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getStub()
    {
        return __DIR__.'/Stubs/repository-facade.stub';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace.'\Domains\Repositories\Facades';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getPath($name)
    {
        return parent::getPath($name.'Facade');
    }
}
