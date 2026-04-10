<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\Client\StoreClientRequest;
use App\Http\Requests\Client\UpdateClientRequest;
use App\Http\Responses\Client\DestroyResponse;
use App\Http\Responses\Client\IndexResponse;
use App\Http\Responses\Client\ShowResponse;
use App\Http\Responses\Client\StoreResponse;
use App\Support\Http\Requests\AppRequest;
use App\Support\Mails\DefaultMail;
use App\UseCases\Client\ClientService;
use App\UseCases\Client\Dtos\ClientDto;
use App\UseCases\Notification\NotificationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * クライアントControllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class ClientController extends Controller
{
    /**
     * クライアント一覧を検索して返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());

        $value = $service->getClients($dto);

        $response = new IndexResponse();
        $response->assign($value->attributes());

        return response()->json($response->attributes());
    }

    /**
     * クライアント詳細を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function show(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());

        $value = $service->show($dto);

        $response = new ShowResponse();
        $response->assign($value->attributes(), [
            'startAt' => 'startAtCarbon',
            'stopAt' => 'stopAtCarbon',
            'createdAtCarbon' => 'createdAtCarbon',
            'updatedAtCarbon' => 'updatedAtCarbon',
        ]);

        return response()->json($response->attributes());
    }

    /**
     * クライアントを登録します。
     *
     * @param StoreClientRequest $request 登録内容
     * @param ClientService $service クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function store(
        StoreClientRequest $request,
        ClientService $service,
        NotificationService $notifications
    ): JsonResponse {
        $executorId = $this->staffIdFromCookie($request);

        $dto = new ClientDto();
        $dto->assign($request->input());
        $dto->executorId = $executorId;

        $value = DB::transaction(function () use ($service, $dto) {
            return $service->store($dto);
        });

        $response = new StoreResponse();
        $response->assign($value->attributes());

        // 全スタッフへ通知を配信
        $notifications->fanOut(
            title: '新しいクライアントが登録されました',
            message: $value->getName() ?? '',
            messageType: 1,
            executorId: $executorId ?? 0,
        );

        //アクセストークンをメール送信します
        send_mail($value->getTo(), new DefaultMail($value));

        return response()->json($response->attributes(), 201);
    }

    /**
     * クライアントを更新します。
     *
     * @param UpdateClientRequest $request 更新内容
     * @param ClientService $service クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function update(UpdateClientRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());
        $dto->executorId = $this->staffIdFromCookie($request);

        $value = DB::transaction(function () use ($service, $dto) {
            return $service->update($dto);
        });

        $response = new StoreResponse();
        $response->assign($value->attributes(), [
            'startAt' => 'startAtCarbon',
            'stopAt' => 'stopAtCarbon',
            'createdAtCarbon' => 'createdAtCarbon',
            'updatedAtCarbon' => 'updatedAtCarbon',
        ]);

        return response()->json($response->attributes());
    }

    /**
     * クライアントを論理削除します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントユースケース
     * @param int $id クライアントID
     * @return JsonResponse JSON レスポンス
     */
    public function destroy(AppRequest $request, ClientService $service, int $id): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());
        $dto->id = $id;
        $dto->executorId = $this->staffIdFromCookie($request);

        DB::transaction(function () use ($service, $dto) {
            $service->destroy($dto);
        });

        $response = new DestroyResponse();

        return response()->json($response->attributes());
    }
}
