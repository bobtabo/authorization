<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\v1;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/**
 * 招待Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\v1
 */
class InvitationController extends Controller
{
    /**
     * 招待 URL を取得する応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request): JsonResponse
    {
        return response()->json([
            'url' => '',
            'message' => 'TODO: 招待URL取得',
        ]);
    }

    /**
     * 招待 URL を発行する応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function issue(Request $request): JsonResponse
    {
        return response()->json([
            'url' => '/auth/invitation/',
            'message' => 'TODO: 招待URL発行',
        ]);
    }
}
