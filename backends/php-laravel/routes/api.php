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
Route::prefix('auth')->controller(AuthController::class)->group(function () {
    Route::get('me', 'getMyProfile');
    Route::get('login', 'login');
    Route::get('invitation/{token}', 'invitation');
    Route::get('logout', 'logout');
});

// --- clients（/clients/store と {id} の衝突を避けるため store を先に定義）---
Route::prefix('clients')->controller(ClientController::class)->group(function () {
    Route::get('/', 'index');
    Route::post('store', 'store');
    Route::put('{id}/update', 'update')->whereNumber('id');
    Route::get('{id}', 'show')->whereNumber('id');
    Route::delete('{id}/delete', 'destroy')->whereNumber('id');
});

// --- staffs ---
Route::prefix('staffs')->controller(StaffController::class)->group(function () {
    Route::get('/', 'index');
    Route::patch('{id}/updateRole', 'updateRole')->whereNumber('id');
    Route::patch('{id}/restore', 'restore')->whereNumber('id');
    Route::delete('{id}/delete', 'destroy')->whereNumber('id');
});

// --- invitation（/invitation/issue を先に）---
Route::prefix('invitation')->controller(InvitationController::class)->group(function () {
    Route::get('issue', 'issue');
    Route::get('/', 'index');
});

// --- gate（OpenAPI: issue のみ bearerAuth）---
Route::prefix('gate')->controller(GateController::class)->group(function () {
    Route::get('issue', 'issue')->middleware('client.token');
    Route::get('client/{identifier}/verify', 'verify')
        ->where('identifier', '[a-zA-Z0-9._-]+');
});

// --- notifications（認証はコントローラー側で処理）---
Route::prefix('notifications')->controller(NotificationController::class)->group(function () {
    Route::get('counts', 'counts');
    Route::get('/', 'index');
    Route::patch('/', 'readAll');
    Route::patch('{id}', 'read')->whereNumber('id');
});
