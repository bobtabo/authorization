<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Mappers;

use AutoMapperPlus\Exception\UnregisteredMappingException;

/**
 * 簡単マッピングを行うユーティリティクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Mappers
 */
class SimpleMapper
{
    /**
     * オブジェクトをマッピングします。
     *
     * @param  mixed  $source  マッピング元オブジェクト
     * @param  string  $destinationClass  マッピング先クラス
     * @return mixed マッピングしたオブジェクト
     * @throws UnregisteredMappingException マッピング例外
     */
    public static function map(mixed $source, string $destinationClass)
    {
        $mapper = new DefaultMapper(get_class($source), $destinationClass);

        return $mapper->getMapper()->map($source, $destinationClass);
    }

    /**
     * 指定プロパティをマッピングします。
     *
     * @param  mixed  $source  マッピング元オブジェクト
     * @param  string  $destinationClass  マッピング先クラス
     * @param  array<string, string>  $properties  指定プロパティ連想配列（マッピング元 => マッピング先）
     * @return mixed マッピングしたオブジェクト
     * @throws UnregisteredMappingException マッピング例外
     */
    public static function mapSpecific(mixed $source, string $destinationClass, array $properties)
    {
        $mapper = new SpecificPropertyMapper(get_class($source), $destinationClass, $properties);

        return $mapper->getMapper()->map($source, $destinationClass);
    }
}
