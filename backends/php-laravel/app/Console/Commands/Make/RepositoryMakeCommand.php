<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * Repository生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands\Make
 */
class RepositoryMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:repository';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'リポジトリ生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'Repository';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getStub()
    {
        return __DIR__ . '/Stubs/repository.stub';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\Infrastructure\Repositories';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getPath($name)
    {
        return parent::getPath($name . 'Repository');
    }
}
