<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Responses\Gate\GateIssueResponse;
use App\Http\Responses\Gate\GateVerifyResponse;
use App\Support\Exceptions\AppException;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Gate\Dtos\GateIssueDto;
use App\UseCases\Gate\Dtos\GateVerifyDto;
use App\UseCases\Gate\GateService;
use Illuminate\Http\JsonResponse;

/**
 * 認可Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class GateController extends Controller
{
    /**
     * クライアント会員向け JWT を発行する応答を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param GateService $service 認可Service
     * @return JsonResponse JSON レスポンス
     */
    public function issue(AppRequest $request, GateService $service): JsonResponse
    {
        $member = $request->query('member');
        if (!is_string($member) || $member === '') {
            throw AppException::badRequest('member_required');
        }

        $dto = new GateIssueDto();
        $dto->memberId = $member;
        $dto->accessToken = $request->bearerToken() ?? '';

        $vo = $service->issueToken($dto);

        $response = new GateIssueResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * JWT を検証し Payload 相当の応答を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param GateService $service 認可Service
     * @param string $identifier クライアント識別名
     * @return JsonResponse JSON レスポンス
     */
    public function verify(AppRequest $request, GateService $service, string $identifier): JsonResponse
    {
        $token = $request->query('token');
        if (!is_string($token) || $token === '') {
            throw AppException::badRequest('token_required');
        }

        $dto = new GateVerifyDto();
        $dto->identifier = $identifier;
        $dto->token = $token;

        $vo = $service->verify($dto);

        $response = new GateVerifyResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }
}
