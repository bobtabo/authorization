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
 * クライアントControllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\v1
 */
class ClientController extends Controller
{
    /**
     * クライアント一覧を返します（未実装の場合は空配列）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request): JsonResponse
    {
        return response()->json([]);
    }

    /**
     * クライアント詳細を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  int  $id  クライアントID
     * @return JsonResponse JSON レスポンス
     */
    public function show(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: クライアント詳細',
            'id' => $id,
        ]);
    }

    /**
     * クライアントを登録します（スタブ応答）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function store(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
        ]);
    }

    /**
     * クライアントを更新します（スタブ応答）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  int  $id  クライアントID
     * @return JsonResponse JSON レスポンス
     */
    public function update(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
            'id' => $id,
        ]);
    }

    /**
     * クライアントを削除します（スタブ応答）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  int  $id  クライアントID
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
