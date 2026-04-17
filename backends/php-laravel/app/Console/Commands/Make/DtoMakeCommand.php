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
 * DTO生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands\Make
 */
class DtoMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:dto';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'DTO生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'Dto';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getStub()
    {
        return __DIR__ . '/Stubs/dto.stub';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\UseCases';
    }
}
