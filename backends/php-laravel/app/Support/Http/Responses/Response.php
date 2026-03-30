<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Http\Responses;

/**
 * レスポンス機能を提供するインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http\Responses
 */
interface Response
{
    /**
     * データを設定します。
     *
     * @param array<string, mixed> $keyValue プロパティと値の連想配列
     * @param array<string, string> $convert 変換元 => 変換先の連想配列
     * @param array<string> $excludes 除外項目
     * @return mixed オブジェクト
     */
    public function assign(array $keyValue, array $convert = [], array $excludes = []): mixed;

    /**
     * 属性を取得します。
     *
     * @return array|string 属性
     */
    public function attributes(): array|string;
}
