<?php
/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */
namespace App\Traits;

use UnitEnum;

/**
 * EnumインスタンスのEnum値変換Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Traits
 */
trait EnumValue
{
    /**
     * Enumを判定し値を取得します。
     *
     * @param mixed $value オブジェクト
     * @return mixed Enum値orオブジェクト値
     */
    public function toValue(mixed $value): mixed
    {
        return ($value instanceof UnitEnum) ? $value->value : $value;
    }

    /**
     * Enumを判定し値を取得します。
     *
     * @param array<string, mixed> $values DtoやEntityのattibutesなどの連想配列
     * @return array<string, mixed> Enum値に変換した連想配列
     */
    public function toValues(array $values): array
    {
        $result = [];
        foreach ($values as $key => $value) {
            $result[$key] = $this->toValue($value);
        }
        return $result;
    }

    /**
     * Enumプロパティを取得します。
     *
     * @param array<string, mixed> $values DtoやEntityのattibutesなどの連想配列
     * @return string[] Enumプロパティ配列
     */
    public function getEnumProperties(array $values): array
    {
        $result = [];
        foreach ($values as $key => $value) {
            if ($value instanceof UnitEnum) {
                $result[] = $key;
            }
        }
        return $result;
    }

    /**
     * Enumが含まれているか確認します。
     *
     * @param array<string, mixed> $values DtoやEntityのattibutesなどの連想配列
     * @return bool Enumが含まれている場合 true を返します
     */
    public function hasEnum(array $values): bool
    {
        return !empty($this->getEnumProperties($values));
    }
}
