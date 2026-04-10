<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

/**
 * LaravelのResponseクラスにマクロを追加するためのファイルです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

namespace Illuminate\Http {

    use Symfony\Component\HttpFoundation\Response as ResponseStatus;

    class Response
    {
        /**
         * 200 OKのレスポンスを返します。
         *
         * @param array $data データ
         * @param int $status HTTPステータスコード
         * @return JsonResponse レスポンス
         */
        public function success(array $data = [], int $status = ResponseStatus::HTTP_OK): \Illuminate\Http\JsonResponse
        {
        }

        /**
         * 404 NOT FOUNDのレスポンスを返します。
         *
         * @param string $message メッセージ
         * @return JsonResponse レスポンス
         */
        public function notFound(string $message = 'データが存在しません。'): \Illuminate\Http\JsonResponse
        {
        }
    }
}
