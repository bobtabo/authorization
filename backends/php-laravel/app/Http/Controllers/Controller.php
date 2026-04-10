<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers;

use Illuminate\Contracts\Encryption\DecryptException;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Crypt;

/**
 * 基底Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers
 */
abstract class Controller
{
    /**
     * staff_id クッキーを復号してスタッフIDを返します。
     * クッキーがない・復号失敗・値が不正な場合は null を返します。
     *
     * @param Request $request HTTP リクエスト
     * @return int|null スタッフID
     */
    protected function staffIdFromCookie(Request $request): ?int
    {
        $encrypted = $request->cookie('staff_id');
        if (empty($encrypted)) {
            return null;
        }

        try {
            $decrypted = Crypt::decrypt($encrypted, false);
            $value = str_contains($decrypted, '|')
                ? substr($decrypted, strrpos($decrypted, '|') + 1)
                : $decrypted;
            $staffId = (int) $value;

            return $staffId > 0 ? $staffId : null;
        } catch (DecryptException) {
            return null;
        }
    }
}
