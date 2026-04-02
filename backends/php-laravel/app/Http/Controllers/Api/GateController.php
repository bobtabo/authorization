<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\UseCases\Gate\Dtos\GateIssueDto;
use App\UseCases\Gate\Dtos\GateVerifyDto;
use App\UseCases\Gate\GateService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

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
     * @param  Request  $request  HTTP リクエスト
     * @param  GateService  $gate  ゲートユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function issue(Request $request, GateService $gate): JsonResponse
    {
        $member = $request->query('member');
        if (! is_string($member) || $member === '') {
            return response()->json(['message' => 'member を指定してください。'], 400);
        }

        $dto = new GateIssueDto;
        $dto->memberId = $member;

        return response()->json($gate->issueToken($dto)->attributes());
    }

    /**
     * JWT を検証し Payload 相当の応答を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  GateService  $gate  ゲートユースケース
     * @param  string  $identifier  クライアント識別名
     * @return JsonResponse JSON レスポンス
     */
    public function verify(Request $request, GateService $gate, string $identifier): JsonResponse
    {
        $token = $request->query('token');
        if (! is_string($token) || $token === '') {
            return response()->json(['message' => 'token を指定してください。'], 400);
        }

        $dto = new GateVerifyDto;
        $dto->identifier = $identifier;
        $dto->token = $token;

        return response()->json($gate->verify($dto)->attributes());
    }
}
