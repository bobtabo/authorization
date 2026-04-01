<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\Client\StoreClientRequest;
use App\Http\Requests\Client\UpdateClientRequest;
use App\Http\Responses\Client\ClientDestroyResponse;
use App\Http\Responses\Client\ClientIndexResponse;
use App\Http\Responses\Client\ClientMutationResponse;
use App\Http\Responses\Client\ClientShowResponse;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Client\ClientService;
use App\UseCases\Client\Dtos\ClientDto;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Auth;

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
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  ClientService  $service  クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto;
        $dto->assign($request->input());

        $value = $service->getClients($dto);

        $response = new ClientIndexResponse;
        $response->assign($value->attributes());

        return response()->json($response->attributes());
    }

    /**
     * クライアント詳細を返します。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  ClientService  $service  クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function show(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto;
        $dto->assign($request->input());

        $value = $service->show($dto);

        $response = new ClientShowResponse;
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
     * @param  StoreClientRequest  $request  登録内容
     * @param  ClientService  $service  クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function store(StoreClientRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto;
        $dto->assign($request->input());
        $dto->executorId = $this->executorId();

        $value = $service->store($dto);

        $response = new ClientMutationResponse;
        $response->assign($value->attributes());

        return response()->json($response->attributes(), 201);
    }

    /**
     * クライアントを更新します。
     *
     * @param  UpdateClientRequest  $request  更新内容
     * @param  ClientService  $service  クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function update(UpdateClientRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto;
        $dto->assign($request->input());
        $dto->executorId = $this->executorId();

        $value = $service->update($dto);

        $response = new ClientMutationResponse;
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
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  ClientService  $service  クライアントユースケース
     * @param  int  $id  クライアントID
     * @return JsonResponse JSON レスポンス
     */
    public function destroy(AppRequest $request, ClientService $service, int $id): JsonResponse
    {
        $dto = new ClientDto;
        $dto->assign($request->input());
        $dto->id = $id;
        $dto->executorId = $this->executorId();

        $value = $service->destroy($dto);

        $response = new ClientDestroyResponse;
        $response->assign($value->attributes());

        return response()->json($response->attributes());
    }

    /**
     * @return int|null 未ログイン等のときは null（0 に落とさない）
     */
    private function executorId(): ?int
    {
        $id = Auth::id();

        return $id !== null ? (int) $id : null;
    }
}
