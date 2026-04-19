<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Mappers;

use AutoMapperPlus\AutoMapper;
use AutoMapperPlus\Configuration\AutoMapperConfig;

/**
 * AutoMapperをカプセル化した標準Mapperクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Mappers
 */
class DefaultMapper
{
    protected string $sourceClassName;

    protected string $destinationClassName;

    protected ?AutoMapperConfig $config;

    protected AutoMapper $mapper;

    /**
     * コンストラクタ
     *
     * @param string $sourceClassName マッピング元クラス
     * @param string $destinationClassName マッピング先クラス
     * @param AutoMapperConfig|null $config マッピング設定
     * @return void
     */
    public function __construct(string $sourceClassName, string $destinationClassName, ?AutoMapperConfig $config = null)
    {
        $this->sourceClassName = $sourceClassName;
        $this->destinationClassName = $destinationClassName;
        $this->config = $config;
    }

    /**
     * マッパー設定を取得します。
     *
     * @return AutoMapperConfig マッパー設定
     */
    public function getConfig(): AutoMapperConfig
    {
        if (empty($this->config)) {
            $this->config = $this->getDefaultConfig();
            $this->config
                ->registerMapping($this->sourceClassName, $this->destinationClassName)
                ->reverseMap();
        }

        return $this->config;
    }

    /**
     * マッパーを取得します。
     *
     * @return AutoMapper マッパーインスタンス
     */
    public function getMapper(): AutoMapper
    {
        if (empty($this->mapper)) {
            $this->mapper = new AutoMapper($this->getConfig());
        }

        return $this->mapper;
    }

    /**
     * 標準マッパー設定を新規に構築します。
     *
     * @return AutoMapperConfig デフォルトオプション付きの設定
     */
    protected function getDefaultConfig(): AutoMapperConfig
    {
        $result = new AutoMapperConfig();
        $result->getOptions()->dontIgnoreNullProperties();

        return $result;
    }
}
