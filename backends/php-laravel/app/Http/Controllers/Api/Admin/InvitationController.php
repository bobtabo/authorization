<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Http\Responses\Invitation\InvitationIssueResponse;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Invitation\Dtos\InvitationDto;
use App\UseCases\Invitation\InvitationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * 管理招待Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api\Admin
 */
class InvitationController extends Controller
{
    /**
     * 招待 URL を発行します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param InvitationService $service 招待Service
     * @return JsonResponse JSON レスポンス
     */
    public function issue(AppRequest $request, InvitationService $service): JsonResponse
    {
        $vo = DB::transaction(function () use ($service) {
            return $service->issue(new InvitationDto());
        });

        $response = new InvitationIssueResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }
}
