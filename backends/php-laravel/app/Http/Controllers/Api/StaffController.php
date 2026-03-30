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
 * スタッフControllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class StaffController extends Controller
{
    /**
     * スタッフ一覧を返します（未実装の場合は空配列）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request): JsonResponse
    {
        return response()->json([]);
    }

    /**
     * スタッフの権限を更新します（スタブ応答）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  int  $id  スタッフID
     * @return JsonResponse JSON レスポンス
     */
    public function updateRole(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
            'id' => $id,
        ]);
    }

    /**
     * スタッフを削除します（スタブ応答）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  int  $id  スタッフID
     * @return JsonResponse JSON レスポンス
     */
    public function destroy(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
            'id' => $id,
        ]);
    }
}
