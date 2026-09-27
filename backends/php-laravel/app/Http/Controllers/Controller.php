<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers;

use App\Support\Http\StaffSession;
use Illuminate\Http\Request;

/**
 * 基底Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers
 */
abstract class Controller
{
    /**
     * 署名済み staff_id クッキーを検証し、スタッフIDを返します。
     * クッキーがない・署名が不正・有効期限切れの場合は null を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return int|null スタッフID
     */
    protected function staffIdFromCookie(Request $request): ?int
    {
        $value = $request->cookie('staff_id');

        return StaffSession::verify($value, config('authorization.app.staff_cookie_secret'));
    }
}
