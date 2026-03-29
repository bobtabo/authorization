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
 * 認可Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\v1
 */
class GateController extends Controller
{
    public function issue(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
        ]);
    }

    public function verify(Request $request, string $identifier): JsonResponse
    {
        return response()->json([
            'message' => 'TODO: JWT 検証 Payload',
            'identifier' => $identifier,
        ]);
    }
}
