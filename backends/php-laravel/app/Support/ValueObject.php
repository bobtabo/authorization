<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support;

/**
 * ValueObject機能を提供するインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * * @package App\Support
 */
interface ValueObject
{
    /**
     * 各フィールドにデータを設定します。
     *
     * @param array<string, mixed> $values データ
     * @param array<string, string> $convert 変換元 => 変換先の連想配列
     * @param array<string> $excludes 除外項目
     * @return mixed オブジェクト
     */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed;
}
