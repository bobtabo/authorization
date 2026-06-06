<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\Staff\DestroyRequest;
use App\Http\Requests\Staff\RestoreRequest;
use App\Http\Requests\Staff\UpdateRoleRequest;
use App\Http\Responses\Staff\StaffIndexResponse;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Staff\Dtos\StaffDto;
use App\UseCases\Staff\StaffService;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * スタッフControllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class StaffController extends Controller
{
    /**
     * スタッフ一覧を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param StaffService $service スタッフService
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, StaffService $service): JsonResponse
    {
        $dto = new StaffDto();
        $dto->assign($request->input());

        $vo = $service->index($dto);

        $response = new StaffIndexResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * スタッフの権限を更新します。
     *
     * @param UpdateRoleRequest $request HTTP リクエスト
     * @param StaffService $service スタッフService
     * @return JsonResponse JSON レスポンス
     */
    public function updateRole(UpdateRoleRequest $request, StaffService $service): JsonResponse
    {
        $dto = new StaffDto();
        $dto->assign($request->input());

        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->updateRole($dto);
        });

        return response()->success(['id' => $vo->getId()]);
    }

    /**
     * スタッフの論理削除を復元します。
     *
     * @param RestoreRequest $request HTTP リクエスト
     * @param StaffService $service スタッフService
     * @return JsonResponse JSONレスポンス
     */
    public function restore(RestoreRequest $request, StaffService $service): JsonResponse
    {
        $dto = new StaffDto();
        $dto->assign($request->input());

        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->restore($dto);
        });

        return response()->success(['id' => $vo->getId()]);
    }

    /**
     * スタッフを論理削除します。
     *
     * @param DestroyRequest $request HTTPリクエスト
     * @param StaffService $service スタッフService
     * @return JsonResponse JSONレスポンス
     */
    public function destroy(DestroyRequest $request, StaffService $service): JsonResponse
    {
        $dto = new StaffDto();
        $dto->assign($request->input());

        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->destroy($dto);
        });

        return response()->success(['id' => $vo->getId()]);
    }

}
