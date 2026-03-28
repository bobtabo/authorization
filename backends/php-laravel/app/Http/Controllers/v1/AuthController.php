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
 * 認証Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\v1
 */
class AuthController extends Controller
{
    public function login(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: ログイン情報の取得を実装',
        ]);
    }

    public function invitation(Request $request, string $token): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 招待トークン検証とログイン情報',
            'token' => $token,
        ]);
    }

    public function googleRedirect(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: Google OAuth リダイレクト',
        ]);
    }

    public function logout(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: ログアウト処理',
        ]);
    }
}
