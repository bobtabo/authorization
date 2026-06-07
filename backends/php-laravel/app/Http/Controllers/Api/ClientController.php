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
use App\Http\Responses\Client\IndexResponse;
use App\Http\Responses\Client\InfoResponse;
use App\Http\Responses\Client\JwtHistoryResponse;
use App\Http\Responses\Client\QrResponse;
use App\Http\Responses\Client\ShowResponse;
use App\Http\Responses\Client\StartResponse;
use App\Http\Responses\Client\StoreResponse;
use App\Support\Http\Requests\AppRequest;
use App\Support\Mails\DefaultMail;
use App\UseCases\Client\ClientService;
use App\UseCases\Client\Dtos\ClientDto;
use App\UseCases\JwtHistory\Dtos\JwtHistoryDto;
use App\UseCases\JwtHistory\JwtHistoryService;
use App\UseCases\Notification\Dtos\NotificationCreateDto;
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
     * @param ClientService $service クライアントService
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
     * @param ClientService $service クライアントService
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

        return response()->success($response->attributes());
    }

    /**
     * クライアントを登録します。
     *
     * @param StoreClientRequest $request 登録内容
     * @param ClientService $clientService クライアントService
     * @param NotificationService $notificationService 通知Service
     * @return JsonResponse JSON レスポンス
     * @throws \Throwable 例外
     */
    public function store(
        StoreClientRequest $request,
        ClientService $clientService,
        NotificationService $notificationService
    ): JsonResponse {
        $executorId = $this->staffIdFromCookie($request);

        $dto = new ClientDto();
        $dto->assign($request->input());
        $dto->executorId = $executorId;

        $value = DB::transaction(function () use ($clientService, $notificationService, $dto) {
            $vo = $clientService->store($dto);

            // 全スタッフへ通知を配信
            $notificationDto = new NotificationCreateDto();
            $notificationDto->assign([
                'messageType' => 1,
                'title' => '新しいクライアントが登録されました',
                'message' => $vo->getName() ?? '',
                'url' => '/clients/show?id=' . $vo->getId(),
                'executorId' => $executorId ?? 0,
            ]);
            $notificationService->fanOut($notificationDto);

            return $vo;
        });

        $response = new StoreResponse();
        $response->assign($value->attributes());

        //アクセストークンをメール送信します
        send_mail($value->getTo(), new DefaultMail($value));

        return response()->success($response->attributes(), 201);
    }

    /**
     * クライアントを更新します。
     *
     * @param UpdateClientRequest $request 更新内容
     * @param ClientService $service クライアントService
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

        return response()->success($response->attributes());
    }

    /**
     * スマホアプリ連携用QRコードデータを返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントService
     * @return JsonResponse JSON レスポンス
     */
    public function qr(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());

        $vo = $service->getQr($dto);

        $response = new QrResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * スマホアプリからの利用開始を処理し、アクセストークンを返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントService
     * @return JsonResponse JSON レスポンス
     */
    public function start(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());

        $vo = DB::transaction(function () use ($service, $dto) {
            return $service->start($dto);
        });

        $response = new StartResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * スマホアプリからの利用停止を処理します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントService
     * @return JsonResponse JSON レスポンス
     */
    public function stop(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());

        DB::transaction(function () use ($service, $dto) {
            $service->stop($dto);
        });

        return response()->success();
    }

    /**
     * スマホアプリ向けにクライアント情報を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントService
     * @return JsonResponse JSON レスポンス
     */
    public function info(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());
        $vo = $service->getInfo($dto);

        $response = new InfoResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * クライアントに紐づくJWT履歴一覧を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param JwtHistoryService $service JWT履歴Service
     * @return JsonResponse JSON レスポンス
     */
    public function jwtHistories(AppRequest $request, JwtHistoryService $service): JsonResponse
    {
        $dto = new JwtHistoryDto();
        $dto->assign($request->input());
        $dto->clientId = (int)$request->route('id');

        $vo = $service->getHistories($dto);

        $response = new JwtHistoryResponse();
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }

    /**
     * クライアントを論理削除します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param ClientService $service クライアントService
     * @return JsonResponse JSON レスポンス
     */
    public function destroy(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto();
        $dto->assign($request->input());
        $dto->executorId = $this->staffIdFromCookie($request);

        DB::transaction(function () use ($service, $dto) {
            $service->destroy($dto);
        });

        return response()->success();
    }
}
