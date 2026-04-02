<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\UseCases\Notification\Dtos\NotificationDto;
use App\UseCases\Notification\NotificationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

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
     * @param  Request  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request, NotificationService $notifications): JsonResponse
    {
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
        $dto->cursor = $cursor;
        $dto->limit = $limit;

        $page = $notifications->listPage($dto);

        return response()->json($page->attributes());
    }

    /**
     * 通知トリガーを受理する応答を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス（202）
     */
    public function store(Request $request): JsonResponse
    {
        $body = $request->all();

        return response()->json([
            'message' => '受理しました（非同期処理は未接続です）。',
            'received' => $body !== [] ? $body : null,
        ], 202);
    }

    /**
     * 通知の一括更新（既読など）の応答を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function bulkPatch(Request $request, NotificationService $notifications): JsonResponse
    {
        $validated = $request->validate([
            'ids' => 'sometimes|array',
            'ids.*' => 'string',
            'all' => 'sometimes|boolean',
        ]);

        $ids = isset($validated['ids']) && is_array($validated['ids'])
            ? array_values(array_filter($validated['ids'], static fn ($v) => is_string($v) && $v !== ''))
            : null;
        $all = (bool) ($validated['all'] ?? false);

        if (($ids === null || $ids === []) && ! $all) {
            return response()->json(['message' => 'ids または all を指定してください。'], 400);
        }

        $dto = new NotificationDto;
        $dto->ids = $ids;
        $dto->all = $all;

        $vo = $notifications->bulkMarkRead($dto);

        return response()->json([
            'message' => $vo->message,
            'updated' => $vo->updated,
        ]);
    }

    /**
     * 通知件数の集計を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function counts(Request $request, NotificationService $notifications): JsonResponse
    {
        return response()->json($notifications->counts(new NotificationDto)->attributes());
    }

    /**
     * 単一通知を更新する応答を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  NotificationService  $notifications  通知ユースケース
     * @param  string  $id  通知ID（UUID）
     * @return JsonResponse JSON レスポンス
     */
    public function update(Request $request, NotificationService $notifications, string $id): JsonResponse
    {
        $attributes = $request->all();

        $dto = new NotificationDto;
        $dto->notificationId = $id;
        $dto->attributes = is_array($attributes) ? $attributes : [];

        $vo = $notifications->patch($dto);

        if (! $vo->ok) {
            return response()->json(['message' => '通知を更新できませんでした。'], 404);
        }

        return response()->json([
            'message' => $vo->message,
            'id' => $vo->id,
        ]);
    }
}
