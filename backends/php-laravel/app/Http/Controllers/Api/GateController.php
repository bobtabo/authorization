<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
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
     * クライアント会員向け JWT を発行する応答を返します（スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function issue(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
        ]);
    }

    /**
     * JWT を検証し Payload 相当の応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  string  $identifier  クライアント識別名
     * @return JsonResponse JSON レスポンス
     */
    public function verify(Request $request, string $identifier): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: JWT 検証 Payload',
            'identifier' => $identifier,
        ]);
    }
}
