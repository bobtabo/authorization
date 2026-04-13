<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Traits;

use App\Support\Exceptions\AppException;
use App\Support\Exceptions\SystemException;

/**
 * オブジェクトのプロパティ取得Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Traits
 */
trait Getter
{
    /**
     * オブジェクトのプロパティを取得します。
     *
     * @param string $name プロパティ名
     * @return mixed プロパティ値
     */
    public function __get(string $name)
    {
        return $this->$name;
    }

    /**
     * オブジェクトのプロパティを取得します。
     *
     * @param string $method メソッド名
     * @param mixed $args 使用禁止
     * @return mixed プロパティ値
     */
    public function __call(string $method, $args)
    {
        if (!empty($args)) {
            AppException::internal('general', ['使用禁止']);
        }

        $name = str($method)
            ->replaceFirst('is', '')
            ->replaceFirst('get', '')
            ->camel()->value();

        return $this->$name;
    }
}
