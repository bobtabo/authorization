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
 * 認証Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class AuthController extends Controller
{
    /**
     * ログイン情報を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function login(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: ログイン情報の取得を実装',
        ]);
    }

    /**
     * 招待トークンを検証しログイン情報を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  string  $token  招待トークン
     * @return JsonResponse JSON レスポンス
     */
    public function invitation(Request $request, string $token): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 招待トークン検証とログイン情報',
            'token' => $token,
        ]);
    }

    /**
     * Google OAuth リダイレクト用の応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function googleRedirect(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: Google OAuth リダイレクト',
        ]);
    }

    /**
     * ログアウト処理の応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function logout(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: ログアウト処理',
        ]);
    }
}
