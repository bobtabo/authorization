<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\ClientController;
use App\Http\Controllers\Api\GateController;
use App\Http\Controllers\Api\InvitationController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\StaffController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API（OpenAPI: docs/api-spec/openapi.yml）
|--------------------------------------------------------------------------
| Laravel の api ルートは既定で /api が付与されます。
| 例: GET /api/auth/login
*/

// --- auth ---
Route::get('auth/me', [AuthController::class, 'getMyProfile']);
Route::get('auth/login', [AuthController::class, 'login']);
Route::get('auth/invitation/{token}', [AuthController::class, 'invitation']);
Route::get('auth/logout', [AuthController::class, 'logout']);

// --- clients（/clients/store と {id} の衝突を避けるため store を先に定義）---
Route::get('clients', [ClientController::class, 'index']);
Route::post('clients/store', [ClientController::class, 'store']);
Route::put('clients/{id}/update', [ClientController::class, 'update'])->whereNumber('id');
Route::get('clients/{id}', [ClientController::class, 'show'])->whereNumber('id');
Route::delete('clients/{id}/delete', [ClientController::class, 'destroy'])->whereNumber('id');

// --- staffs ---
Route::get('staffs', [StaffController::class, 'index']);
Route::patch('staffs/{id}/updateRole', [StaffController::class, 'updateRole'])->whereNumber('id');
Route::patch('staffs/{id}/restore', [StaffController::class, 'restore'])->whereNumber('id');
Route::delete('staffs/{id}/delete', [StaffController::class, 'destroy'])->whereNumber('id');

// --- invitation（/invitation/issue を先に）---
Route::get('invitation/issue', [InvitationController::class, 'issue']);
Route::get('invitation', [InvitationController::class, 'index']);

// --- gate（OpenAPI: issue のみ bearerAuth）---
Route::get('gate/issue', [GateController::class, 'issue'])->middleware('auth:sanctum');
Route::get('gate/client/{identifier}/verify', [GateController::class, 'verify'])
    ->where('identifier', '[a-zA-Z0-9._-]+');

// --- notifications（認証は X-Executor-Id ヘッダーでコントローラー側が処理）---
Route::get('notifications/counts', [NotificationController::class, 'counts']);
Route::get('notifications', [NotificationController::class, 'index']);
Route::post('notifications', [NotificationController::class, 'store']);
Route::patch('notifications', [NotificationController::class, 'bulkPatch']);
Route::patch('notifications/{id}', [NotificationController::class, 'update'])->whereNumber('id');
