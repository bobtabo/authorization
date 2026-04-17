<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Responses\Invitation\InvitationIndexResponse;
use App\Http\Responses\Invitation\InvitationIssueResponse;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Invitation\Dtos\InvitationDto;
use App\UseCases\Invitation\InvitationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * 招待Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class InvitationController extends Controller
{
    /**
     * 現在の招待 URL を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param InvitationService $service 招待Service
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, InvitationService $service): JsonResponse
    {
        $vo = $service->current(new InvitationDto());

        $response = new InvitationIndexResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

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
