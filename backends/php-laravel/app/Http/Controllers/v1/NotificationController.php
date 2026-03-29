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
    public function index(Request $request): JsonResponse
    {
        return response()->json([
            'items' => [],
            'next_cursor' => null,
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 通知トリガー受理',
        ], 202);
    }

    public function bulkPatch(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 一括既読',
        ]);
    }

    public function counts(Request $request): JsonResponse
    {
        return response()->json([
            'unread' => 0,
            'total' => 0,
        ]);
    }

    public function update(Request $request, string $id): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: 単一通知更新',
            'id' => $id,
        ]);
    }
}
