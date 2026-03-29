<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Traits;

use Illuminate\Support\Collection;
use ReflectionClass;

/**
 * 初期化Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Traits
 */
trait Initialize
{
    /**
     * 初期化します。
     *
     * @return void
     */
    public function initializer(): void
    {
        $clazz = new ReflectionClass($this);
        $properties = $clazz->getProperties();
        foreach ($properties as $property) {
            //Nullを許容する場合は何もしない
            if (empty($property->getType()) || $property->getType()->allowsNull()) {
                continue;
            }

            //コレクションの場合は初期化する
            if ($property->getType()->getName() === Collection::class) {
                $property->setAccessible(true);
                $property->setValue($this, collect());
            }
        }
    }
}
