<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;

/**
 * Entity生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands\Make
 */
class EntityMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:entity';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'エンティティ生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'Entity';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getStub()
    {
        return __DIR__.'/Stubs/entity.stub';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace.'\Domains\Entities';
    }
}
