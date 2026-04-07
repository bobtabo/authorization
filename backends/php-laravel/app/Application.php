<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App;

use Illuminate\Foundation\Application as BaseApplication;

/**
 * 拡張Applicationクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App
 */
class Application extends BaseApplication
{
    /**
     * 検証環境であるか確認します。
     *
     * @return bool 検証環境の場合 true を返します
     */
    public function isStaging(): bool
    {
        return $this['env'] === 'staging';
    }

    /**
     * 開発環境であるか確認します。
     *
     * @return bool 検証環境の場合 true を返します
     */
    public function isDevelop(): bool
    {
        return $this['env'] === 'develop';
    }

    /**
     * テスト環境であるか確認します。
     *
     * @return bool テスト環境の場合 true を返します
     */
    public function isTesting(): bool
    {
        return $this['env'] === 'testing';
    }

    /**
     * デバッグONであるか確認します。
     *
     * @return bool デバッグONの場合 true を返します
     */
    public function isDebug(): bool
    {
        return config('app.debug');
    }

    /**
     * 実行環境を表示します
     *
     * @return string 実行環境
     */
    public function display(): string
    {
        $result = '';

        if ($this->isLocal()) {
            $result = 'Local';
        } elseif ($this->isTesting()) {
            $result = 'Test';
        } elseif ($this->isDevelop()) {
            $result = 'Develop';
        } elseif ($this->isStaging()) {
            $result = 'Staging';
        }

        return $result;
    }
}
