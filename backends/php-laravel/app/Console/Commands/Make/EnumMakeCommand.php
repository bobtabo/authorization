<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Console\Commands\Make;

use Illuminate\Console\GeneratorCommand;
use Symfony\Component\Console\Input\InputOption;

/**
 * Enum生成Commandクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Console\Commands\Make
 */
class EnumMakeCommand extends GeneratorCommand
{
    /**
     * The console command name.
     *
     * @var string
     */
    protected $name = 'make:enum';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Enum生成';

    /**
     * The type of class being generated.
     *
     * @var string
     */
    protected $type = 'Enum';

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getStub()
    {
        return __DIR__ . '/Stubs/enum.stub';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getDefaultNamespace($rootNamespace)
    {
        return $rootNamespace . '\Domain';
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getOptions()
    {
        return [
            [
                'value',
                'value',
                InputOption::VALUE_REQUIRED,
                'Enum値の型',
                null,
            ],
        ];
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function replaceClass($stub, $name)
    {
        $result = parent::replaceClass($stub, $name);

        $valueType = $this->option('value');
        if (empty($valueType)) {
            return $result;
        }

        return str_replace(['{{ value }}', '{{value}}'], $valueType, $result);
    }
}
