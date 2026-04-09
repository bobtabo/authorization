<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Responses\Notification\BulkPatchResponse;
use App\Http\Responses\Notification\CountsResponse;
use App\Http\Responses\Notification\IndexResponse;
use App\Http\Responses\Notification\IssueResponse;
use App\Http\Responses\Notification\UpdateResponse;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Notification\Dtos\NotificationDto;
use App\UseCases\Notification\NotificationService;
use Illuminate\Http\JsonResponse;

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
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, NotificationService $notifications): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if (empty($staffId)) {
            return response()->json(['message' => '未認証です。'], 401);
        }

        $cursor = $request->query('cursor');
        $cursor = is_string($cursor) && $cursor !== '' ? $cursor : null;

        $limit = (int) $request->query('limit', 20);
        if ($limit < 1) {
            $limit = 1;
        }
        if ($limit > 100) {
            $limit = 100;
        }

        $dto = new NotificationDto;
        $dto->staffId = (int) $staffId;
        $dto->cursor = $cursor;
        $dto->limit = $limit;

        $vo = $notifications->listPage($dto);

        $response = new IndexResponse;
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }

    /**
     * 通知トリガーを受理する応答を返します。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス（202）
     */
    public function store(AppRequest $request): JsonResponse
    {
        $body = $request->all();

        $response = new IssueResponse;
        $response->assign([
            'message' => '受理しました（非同期処理は未接続です）。',
            'received' => $body !== [] ? $body : null,
        ]);

        return response()->json($response->attributes(), 202);
    }

    /**
     * 通知の一括更新（既読など）の応答を返します。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function bulkPatch(AppRequest $request, NotificationService $notifications): JsonResponse
    {
        $staffId = $request->all()['executor_id'] ?? null;
        if (empty($staffId)) {
            return response()->json(['message' => '未認証です。'], 401);
        }

        $validated = $request->validate([
            'ids' => 'sometimes|array',
            'ids.*' => 'integer',
            'all' => 'sometimes|boolean',
        ]);

        $ids = isset($validated['ids']) && is_array($validated['ids'])
            ? array_values(array_filter(array_map('intval', $validated['ids']), static fn ($v) => $v > 0))
            : null;
        $all = (bool) ($validated['all'] ?? false);

        if (($ids === null || $ids === []) && ! $all) {
            return response()->json(['message' => 'ids または all を指定してください。'], 400);
        }

        $dto = new NotificationDto;
        $dto->staffId = (int) $staffId;
        $dto->ids = $ids;
        $dto->all = $all;

        $vo = $notifications->bulkMarkRead($dto);

        $response = new BulkPatchResponse;
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }

    /**
     * 通知件数の集計を返します。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function counts(AppRequest $request, NotificationService $notifications): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if (empty($staffId)) {
            return response()->json(['message' => '未認証です。'], 401);
        }

        $dto = new NotificationDto;
        $dto->staffId = (int) $staffId;

        $vo = $notifications->counts($dto);

        $response = new CountsResponse;
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }

    /**
     * 単一通知を更新する応答を返します。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @param  int  $id  通知ID
     * @return JsonResponse JSON レスポンス
     */
    public function update(AppRequest $request, NotificationService $notifications, int $id): JsonResponse
    {
        $attributes = $request->all();

        $dto = new NotificationDto;
        $dto->notificationId = $id;
        $dto->attributes = is_array($attributes) ? $attributes : [];

        $vo = $notifications->patch($dto);

        if (! $vo->isOk()) {
            return response()->json(['message' => '通知を更新できませんでした。'], 404);
        }

        $response = new UpdateResponse;
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }
}
