<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Exceptions;

use RuntimeException;
use Symfony\Component\HttpFoundation\Response;

/**
 * アプリケーションExceptionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Exceptions
 */
class AppException extends RuntimeException
{
    /**
     * コンストラクタ。
     *
     * @param int $status HTTPステータスコード
     * @param string $key メッセージキー
     * @param array $replace メッセージ置換文字列
     */
    public function __construct(
        int $status,
        string $key,
        array $replace = [],
    ) {
        $message = __('validation.custom.' . $key, $replace);
        parent::__construct($message, $status);
    }

    /**
     * 不正リクエスト例外を取得します。
     *
     * @param string $key
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function badRequest(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_BAD_REQUEST, $key, $replace);
    }

    /**
     * 認証エラー例外を取得します。
     *
     * @param string $key エラーメッセージキー
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function unauthorized(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_UNAUTHORIZED, $key, $replace);
    }

    /**
     * アクセス拒否の例外を取得します。
     *
     * @param string $key エラーメッセージキー
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function forbidden(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_FORBIDDEN, $key, $replace);
    }

    /**
     * リソース未検出の例外を取得します。
     *
     * @param string $key エラーメッセージキー
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function notFound(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_NOT_FOUND, $key, $replace);
    }

    /**
     * リクエスト制限の例外を取得します。
     *
     * @param string $key エラーメッセージキー
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function manyRequest(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_TOO_MANY_REQUESTS, $key, $replace);
    }

    /**
     * サーバー内部エラー例外を取得します。
     *
     * @param string $key エラーメッセージキー
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function internal(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_INTERNAL_SERVER_ERROR, $key, $replace);
    }

    /**
     * サービス利用不可の例外を取得します。
     *
     * @param string $key エラーメッセージキー
     * @param array $replace メッセージ置換文字列
     * @return self 例外
     */
    public static function unavailable(string $key, array $replace = []): self
    {
        return new self(Response::HTTP_SERVICE_UNAVAILABLE, $key, $replace);
    }
}
