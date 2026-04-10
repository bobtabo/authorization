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
     * @param InvitationService $invitations 招待ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, InvitationService $invitations): JsonResponse
    {
        $vo = $invitations->current(new InvitationDto());
        if (!$vo->isFound()) {
            return response()->failure(__('validation.custom.invitation_not_found'));
        }

        $response = new InvitationIndexResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * 招待 URL を発行します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param InvitationService $invitations 招待ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function issue(AppRequest $request, InvitationService $invitations): JsonResponse
    {
        $vo = DB::transaction(function () use ($invitations) {
            return $invitations->issue(new InvitationDto());
        });

        $response = new InvitationIssueResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }
}
