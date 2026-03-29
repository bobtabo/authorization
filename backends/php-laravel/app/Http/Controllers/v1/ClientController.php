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
    public function index(Request $request): JsonResponse
    {
        return response()->json([]);
    }

    public function show(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: クライアント詳細',
            'id' => $id,
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
        ]);
    }

    public function update(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
            'id' => $id,
        ]);
    }

    public function destroy(Request $request, int $id): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
            'id' => $id,
        ]);
    }
}
