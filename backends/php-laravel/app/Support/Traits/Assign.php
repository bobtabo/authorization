<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Traits;

use Carbon\Carbon;
use Exception;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;
use ReflectionClass;
use UnitEnum;

/**
 * プロパティ設定Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Traits
 */
trait Assign
{
    /**
     * 配列を各プロパティに設定します。
     *
     * @param array<string, mixed> $values キーと値の連想配列
     * @param array<string, string> $convert 変換元 => 変換先の連想配列
     * @param array<string> $excludes 除外項目
     * @return mixed オブジェクト
     */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed
    {
        $clazz = new ReflectionClass($this);
        foreach ($values as $key => $value) {
            if (in_array($key, $excludes, true)) {
                continue;
            }

            $key = $this->convertProperty($key, $convert);

            if ($clazz->hasProperty($key)) {
                $targetProperty = $key;
            } else {
                $camel = Str::camel(Str::lower($key));

                if (!$clazz->hasProperty($camel)) {
                    continue;
                }

                $targetProperty = $camel;
            }

            $property = $clazz->getProperty($targetProperty);
            $property->setAccessible(true);
            $property->setValue($this, $this->getAssignValue($clazz, $targetProperty, $value));
        }

        return $this;
    }

    /**
     * 設定値を取得します。
     *
     * @param ReflectionClass $targetClass 対象クラス
     * @param string $targetProperty 対象プロパティ
     * @param mixed $value 設定値
     * @return bool|mixed|Carbon|UnitEnum|null 調整した設定値
     */
    private function getAssignValue(ReflectionClass $targetClass, string $targetProperty, mixed $value)
    {
        $result = null;

        try {
            $property = $targetClass->getProperty($targetProperty);
            $typeClass = $property->getType()->getName();
            if ($typeClass == Carbon::class) {
                //対象プロパティがCarbonの場合
                if (!empty($value)) {
                    $result = Carbon::parse($value);
                }
            } elseif ($this->isEnum($typeClass) && !($value instanceof UnitEnum)) {
                //対象プロパティがEnumで設定値がEnumではない場合
                $method = (new ReflectionClass($typeClass))->getMethod('from');
                if (!is_null($value)) {
                    $result = $method->invoke(null, $value);
                }
            } elseif (!$this->isEnum($typeClass) && ($value instanceof UnitEnum)) {
                //対象プロパティがEnumではなく設定値がEnumの場合
                $result = $value->value;
            } elseif (is_bool($property->getValue($this))) {
                //対象プロパティがBoolの場合
                $result = (bool) $value;
            } else {
                //以外
                $result = $value;
            }
        } catch (Exception $e) {
            Log::debug($e);
            $result = $value;
        }

        return $result;
    }

    /**
     * 対象プロパティを変換します。
     *
     * @param string $property 対象プロパティ
     * @param array<string, string> $convert 変換元 => 変換先の連想配列
     * @return string 変換したプロパティ
     */
    private function convertProperty(string $property, array $convert = []): string
    {
        if (empty($convert)) {
            return $property;
        }
        if (!Arr::exists($convert, $property)) {
            return $property;
        }
        return $convert[$property];
    }

    /**
     * 対象クラスがEnumであるか確認します。
     *
     * @param string $clazz 対象クラス
     * @return bool Enumの場合 true を返します
     */
    private function isEnum(string $clazz): bool
    {
        try {
            $interfaces = (new ReflectionClass($clazz))->getInterfaceNames();
            foreach ($interfaces as $interface) {
                if ($interface == 'UnitEnum') {
                    return true;
                }
            }
        } catch (Exception $e) {
            return false;
        }
        return false;
    }
}
