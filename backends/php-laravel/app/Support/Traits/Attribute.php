<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Traits;

use ReflectionClass;

/**
 * 属性取得Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Traits
 */
trait Attribute
{
    /**
     * 属性を取得します。
     *
     * @return array<string, mixed> 属性の配列
     */
    public function attributes(): array
    {
        $result = [];

        $clazz = new ReflectionClass($this);
        $properties = $clazz->getProperties();
        foreach ($properties as $property) {
            $key = $property->getName();
            $result[$key] = $property->getValue($this);
        }

        return $result;
    }

    /**
     * スネーク属性を取得します。
     *
     * @return array<string, mixed> スネーク属性の配列
     */
    public function attributesBySnake(): array
    {
        $result = [];
        foreach ($this as $key => $value) {
            $snake = str($key)->snake()->value();
            $result[$snake] = $value;
        }
        return $result;
    }
}
