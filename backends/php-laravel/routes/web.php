<?php

use App\Http\Controllers\Api\AuthController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

Route::prefix('auth/google')->controller(AuthController::class)->group(function () {
    Route::get('redirect', 'googleRedirect');
    Route::get('callback', 'googleCallback');
});

Route::prefix('auth/github')->controller(AuthController::class)->group(function () {
    Route::get('redirect', 'githubRedirect');
    Route::get('callback', 'githubCallback');
});
