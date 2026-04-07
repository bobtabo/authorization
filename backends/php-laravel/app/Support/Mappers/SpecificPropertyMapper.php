<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Mappers;

use AutoMapperPlus\Configuration\AutoMapperConfig;
use AutoMapperPlus\Configuration\Mapping;

/**
 * 指定プロパティのAutoMapperクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Mappers
 */
class SpecificPropertyMapper extends DefaultMapper
{
    /**
     * @var array 指定プロパティ連想配列
     */
    private array $properties;

    /**
     * コンストラクタ
     *
     * @param  string  $sourceClassName  マッピング元クラス
     * @param  string  $destinationClassName  マッピング先クラス
     * @param  array<string, string>  $properties  指定プロパティ連想配列（マッピング元 => マッピング先）
     * @param  AutoMapperConfig|null  $config  マッピング設定
     * @return void
     */
    public function __construct(
        string $sourceClassName,
        string $destinationClassName,
        array $properties,
        ?AutoMapperConfig $config = null
    ) {
        parent::__construct($sourceClassName, $destinationClassName, $config);

        $this->properties = $properties;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function getConfig(): AutoMapperConfig
    {
        $this->config = $this->getDefaultConfig();

        /** @var Mapping $mapping */
        $mapping = $this->config->registerMapping($this->sourceClassName, $this->destinationClassName);

        foreach ($this->properties as $sourceProperty => $destProperty) {
            $mapping->forMember($destProperty, function ($source) use ($sourceProperty) {
                return $source->$sourceProperty;
            });
        }

        $mapping->reverseMap();

        return $this->config;
    }
}
