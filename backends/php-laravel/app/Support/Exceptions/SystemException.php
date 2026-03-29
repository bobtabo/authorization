<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Exceptions;

/**
 * システムExceptionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Exceptions
 */
class SystemException extends AppException
{
    protected static string $errorType = 'system';

    /**
     * 汎用メッセージ
     */
    const GENERAL = 'general';

    /**
     * データが見つからない
     */
    const NOT_FOUND = 'not_found';
}
