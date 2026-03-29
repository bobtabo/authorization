<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

use App\Http\Controllers\v1\AccountController;
use App\Http\Controllers\v1\AuthController;
use App\Http\Controllers\v1\ClientController;
use App\Http\Controllers\v1\GateController;
use App\Http\Controllers\v1\InvitationController;
use App\Http\Controllers\v1\NotificationController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API v1（OpenAPI: docs/api-spec/openapi.yml）
|--------------------------------------------------------------------------
| Laravel の api ルートは既定で /api が付与されます。
| 例: GET /api/v1/auth/login
*/

Route::prefix('v1')->group(function () {
    // --- auth ---
    Route::get('auth/login', [AuthController::class, 'login']);
    Route::get('auth/invitation/{token}', [AuthController::class, 'invitation']);
    Route::get('auth/google/redirect', [AuthController::class, 'googleRedirect']);
    Route::get('auth/logout', [AuthController::class, 'logout']);

    // --- clients（{id} は数値のみ。/clients/store と衝突しないよう store を先に定義）---
    Route::get('clients', [ClientController::class, 'index']);
    Route::post('clients/store', [ClientController::class, 'store']);
    Route::get('clients/{id}', [ClientController::class, 'show'])->whereNumber('id');
    Route::put('clients/{id}/update', [ClientController::class, 'update'])->whereNumber('id');
    Route::delete('clients/{id}/delete', [ClientController::class, 'destroy'])->whereNumber('id');

    // --- accounts ---
    Route::get('accounts', [AccountController::class, 'index']);
    Route::patch('accounts/{id}/updateRole', [AccountController::class, 'updateRole'])->whereNumber('id');
    Route::delete('accounts/{id}/delete', [AccountController::class, 'destroy'])->whereNumber('id');

    // --- invitation（/invitation/issue を先に）---
    Route::get('invitation/issue', [InvitationController::class, 'issue']);
    Route::get('invitation', [InvitationController::class, 'index']);

    // --- gate（OpenAPI: issue のみ bearerAuth）---
    Route::get('gate/issue', [GateController::class, 'issue'])->middleware('auth:sanctum');
    Route::get('gate/client/{identifier}/verify', [GateController::class, 'verify'])
        ->where('identifier', '[a-zA-Z0-9._-]+');

    // --- notifications（OpenAPI: POST トリガー以外は主に bearerAuth）---
    Route::get('notifications/counts', [NotificationController::class, 'counts'])->middleware('auth:sanctum');
    Route::get('notifications', [NotificationController::class, 'index'])->middleware('auth:sanctum');
    Route::post('notifications', [NotificationController::class, 'store']);
    Route::patch('notifications', [NotificationController::class, 'bulkPatch'])->middleware('auth:sanctum');
    Route::patch('notifications/{id}', [NotificationController::class, 'update'])->middleware('auth:sanctum')->whereUuid('id');
});
