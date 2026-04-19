<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers;

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
     * staff_id クッキーからスタッフIDを返します。
     * クッキーがない・値が不正な場合は null を返します。
     *
     * @param Request $request HTTP リクエスト
     * @return int|null スタッフID
     */
    protected function staffIdFromCookie(Request $request): ?int
    {
        $value = $request->cookie('staff_id');
        if (empty($value)) {
            return null;
        }

        $staffId = (int) $value;
        return $staffId > 0 ? $staffId : null;
    }
}
