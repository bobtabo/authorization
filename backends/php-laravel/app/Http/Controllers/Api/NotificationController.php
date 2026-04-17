<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Responses\Notification\CountsResponse;
use App\Http\Responses\Notification\IndexResponse;
use App\Support\Exceptions\AppException;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Notification\Dtos\NotificationDto;
use App\UseCases\Notification\NotificationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * 通知Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class NotificationController extends Controller
{
    /**
     * 通知一覧（カーソルページング）を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param NotificationService $service 通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, NotificationService $service): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if (empty($staffId)) {
            throw AppException::unauthorized('unauthenticated');
        }

        $cursor = $request->query('cursor');
        $cursor = is_string($cursor) && $cursor !== '' ? $cursor : null;

        $limit = (int)$request->query('limit', config('authorization.app.notification_default_limit'));
        if ($limit < 1) {
            $limit = 1;
        }

        $dto = new NotificationDto();
        $dto->staffId = (int)$staffId;
        $dto->cursor = $cursor;
        $dto->limit = $limit;

        $vo = $service->listPage($dto);

        $response = new IndexResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * 通知トリガーを受理する応答を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @return JsonResponse JSON レスポンス（202）
     */
    public function store(AppRequest $request): JsonResponse
    {
        $body = $request->all();

        return response()->success([
            'message' => __('validation.custom.notification_accepted'),
            'received' => $body !== [] ? $body : null,
        ], 202);
    }

    /**
     * 通知の一括更新（既読など）の応答を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param NotificationService $service 通知Service
     * @return JsonResponse JSON レスポンス
     */
    public function readAll(AppRequest $request, NotificationService $service): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if (empty($staffId)) {
            throw AppException::unauthorized('unauthenticated');
        }

        $dto = new NotificationDto();
        $dto->assign([
            'staffId' => $staffId,
        ]);

        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->reads($dto);
        });

        return response()->success(['updated' => $vo->getUpdated()]);
    }

    /**
     * 通知件数の集計を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param NotificationService $service 通知Service
     * @return JsonResponse JSON レスポンス
     */
    public function counts(AppRequest $request, NotificationService $service): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if (empty($staffId)) {
            throw AppException::unauthorized('unauthenticated');
        }

        $dto = new NotificationDto();
        $dto->staffId = (int)$staffId;

        $vo = $service->counts($dto);

        $response = new CountsResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * 単一通知を更新する応答を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param NotificationService $service 通知Service
     * @return JsonResponse JSON レスポンス
     */
    public function read(AppRequest $request, NotificationService $service): JsonResponse
    {
        $dto = new NotificationDto();
        $dto->assign($request->all(), [
            'id' => 'notificationId',
        ]);

        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->read($dto);
        });

        return response()->success(['id' => $vo->getId()]);
    }
}
