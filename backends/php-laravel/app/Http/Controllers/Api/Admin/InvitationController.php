<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Http\Responses\Invitation\InvitationIndexResponse;
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
     * 現在の招待 URL を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param InvitationService $service 招待Service
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, InvitationService $service): JsonResponse
    {
        $dto = new InvitationDto();
        $dto->role = $this->resolveRole($request);
        $vo = $service->current($dto);

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
        $executorId = $this->staffIdFromCookie($request);
        if ($executorId === null) {
            throw \App\Support\Exceptions\AppException::unauthorized('unauthenticated');
        }

        $dto = new InvitationDto();
        $dto->role = $this->resolveRole($request);
        $dto->executorId = $executorId;
        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->issue($dto);
        });

        $response = new InvitationIssueResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * クエリパラメーターから role を取得します（1 or 2、それ以外は 400）。
     *
     * @param AppRequest $request HTTP リクエスト
     * @return int 権限（1=管理者, 2=メンバー）
     */
    private function resolveRole(AppRequest $request): int
    {
        $raw = $request->query('role');
        if ($raw === null) {
            return 2;
        }
        if (!ctype_digit((string)$raw)) {
            throw \App\Support\Exceptions\AppException::badRequest('invalid_role');
        }
        $role = (int)$raw;
        if (!in_array($role, [1, 2], true)) {
            throw \App\Support\Exceptions\AppException::badRequest('invalid_role');
        }
        return $role;
    }
}
