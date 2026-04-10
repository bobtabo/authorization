<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Illuminate\Http {

    use Symfony\Component\HttpFoundation\Response as ResponseStatus;

    class Response
    {
        /**
         * @param array|object $data
         * @param int $status
         * @return \Illuminate\Http\JsonResponse
         */
        public function success($data, int $status = ResponseStatus::HTTP_OK): \Illuminate\Http\JsonResponse
        {
        }
    }
}
