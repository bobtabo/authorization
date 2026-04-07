<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Responses;

/**
 * エラーレスポンス機能を提供するインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Http\Responses
 */
interface ErrorResponse
{
    /**
     * エラーメッセージを取得します。
     *
     * @return string エラーメッセージ
     */
    public function getMessage(): string;

    /**
     * エラーコードを取得します。
     *
     * @return int エラーコード
     */
    public function getCode(): int;

    /**
     * HTTPステータスコードを取得します。
     *
     * @return int HTTPステータ
     */
    public function getStatusCode(): int;
}
