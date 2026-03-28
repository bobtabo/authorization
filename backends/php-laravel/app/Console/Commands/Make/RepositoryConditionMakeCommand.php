<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * リポジトリ検索条件の生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands\Make
 */
class RepositoryConditionMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:repository-condition';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'リポジトリ検索条件を生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'RepositoryCondition';

    /**
     * {@inheritdoc}
     */
    protected function getStub()
    {
        return __DIR__ . '/Stubs/repository-condition.stub';
    }

    /**
     * {@inheritdoc}
     */
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\Domains\Repositories\Conditions';
    }

    /**
     * {@inheritdoc}
     */
    protected function getPath($name)
    {
        return parent::getPath($name . 'Condition');
    }
}
