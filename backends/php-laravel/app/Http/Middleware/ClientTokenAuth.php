<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Middleware;

use App\Support\Exceptions\AppException;
use App\UseCases\Client\ClientService;
use App\UseCases\Client\Dtos\ClientDto;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * クライアントのアクセストークンを検証するミドルウェアです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Middleware
 */
class ClientTokenAuth
{
    /**
     * @param ClientService $clientService クライアントサービス
     */
    public function __construct(
        private readonly ClientService $clientService,
    ) {
    }

    /**
     * リクエストのBearerトークンでクライアントを認証します。
     *
     * @throws AppException 認証失敗の場合
     */
    public function handle(Request $request, Closure $next): Response
    {
        $dto = new ClientDto();
        $dto->accessToken = $request->bearerToken() ?? '';

        if (!$this->clientService->authenticateByToken($dto)) {
            throw AppException::unauthorized('client_not_found');
        }

        return $next($request);
    }
}
