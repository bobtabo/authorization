<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Responses;

use App\Support\Traits\Assign;
use App\Support\Traits\Initialize;
use Illuminate\Support\Str;
use ReflectionClass;

/**
 * 基底Responseクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http\Responses
 *
 * @method int|null getVersion()
 */
abstract class AbstractResponse implements Response
{
    use Assign;
    use Initialize;

    protected ?int $version = null;

    /**
     * コンストラクタ
     */
    public function __construct()
    {
        $this->initializer();
    }

    /**
     * @inheritdoc}
     */
    #[\Override]
    public function attributes(): array|string
    {
        $result = [];
        foreach ($this->getResponseKeys() as $property) {
            if (array_key_exists($property, $this->getExcludeKeys())) {
                continue;
            }

            $result[Str::snake($property)] = $this->$property;
        }
        return $result;
    }

    /**
     * レスポンス出力キーを取得します。
     *
     * @return string[] レスポンス出力キー配列
     */
    protected function getResponseKeys(): array
    {
        $properties = (new ReflectionClass($this))->getProperties();
        $result = [];
        foreach ($properties as $property) {
            $result[] = $property->getName();
        }

        return $result;
    }

    /**
     * レスポンスから除外するキーを取得します。
     *
     * @return string[] レスポンスから除外するキー配列
     */
    protected function getExcludeKeys(): array
    {
        return [];
    }
}
