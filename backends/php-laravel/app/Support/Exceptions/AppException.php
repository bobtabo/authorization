<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Exceptions;

use Lang;
use RuntimeException;
use Symfony\Component\HttpFoundation\Response;

/**
 * 基底Exceptionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Exceptions
 */
class AppException extends RuntimeException
{
    public ?string $redirectTo = null;

    protected array $responses = [];

    protected static string $errorType = '';

    const string DEFAULT = 'default';

    /**
     * コンストラクタ。
     *
     * @param string|null $errorKey エラーキー
     * @param array|null $replace メッセージ置換文字列
     * @param int|null $statusCode ステータスコード
     * @param array<int, mixed> $responses 追加レスポンス
     */
    public function __construct(
        ?string $errorKey = self::DEFAULT,
        ?array $replace = [],
        ?int $statusCode = Response::HTTP_INTERNAL_SERVER_ERROR,
        ...$responses
    ) {
        if (empty(static::$errorType)) {
            $message = Lang::get('exception.' . $errorKey, $replace);
        } else {
            $message = Lang::get('exception.' . static::$errorType . '.' . $errorKey, $replace);
        }

        parent::__construct($message, $statusCode);
        $this->responses = $responses;
    }

    /**
     * 追加レスポンスを取得します。
     *
     * @return array<int, mixed> 追加レスポンス
     */
    final public function getResponses(): array
    {
        return $this->responses;
    }
}
