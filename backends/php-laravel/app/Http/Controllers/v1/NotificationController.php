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
 * 通知Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\v1
 */
class NotificationController extends Controller
{
    /**
     * 通知一覧（カーソルページング）を返します（スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request): JsonResponse
    {
        return response()->json([
            'items' => [],
            'next_cursor' => null,
        ]);
    }

    /**
     * 通知トリガーを受理する応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス（202）
     */
    public function store(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 通知トリガー受理',
        ], 202);
    }

    /**
     * 通知の一括更新（既読など）の応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function bulkPatch(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 一括既読',
        ]);
    }

    /**
     * 通知件数の集計を返します（スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function counts(Request $request): JsonResponse
    {
        return response()->json([
            'unread' => 0,
            'total' => 0,
        ]);
    }

    /**
     * 単一通知を更新する応答を返します（未実装スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  string  $id  通知ID（UUID）
     * @return JsonResponse JSON レスポンス
     */
    public function update(Request $request, string $id): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 単一通知更新',
            'id' => $id,
        ]);
    }
}
