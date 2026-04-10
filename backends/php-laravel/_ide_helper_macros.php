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
         * 正常（200）のレスポンスを返します。
         *
         * @param array $data データ
         * @param int $status HTTPステータスコード
         * @return JsonResponse レスポンス
         */
        public function success(array $data = [], int $status = ResponseStatus::HTTP_OK): \Illuminate\Http\JsonResponse
        {
        }

        /**
         * エラー（4xx、5xx）のレスポンスを返します。
         *
         * @param string $message メッセージ
         * @param int $status HTTPステータスコード
         * @return JsonResponse レスポンス
         */
        public function failure(
            string $message = 'データが存在しません。',
            int $status = ResponseStatus::HTTP_NOT_FOUND
        ): \Illuminate\Http\JsonResponse {
        }
    }
}
