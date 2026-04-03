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
use App\Http\Responses\Client\DestroyResponse;
use App\Http\Responses\Client\IndexResponse;
use App\Http\Responses\Client\ShowResponse;
use App\Http\Responses\Client\StoreResponse;
use App\Support\Http\Requests\AppRequest;
use App\Support\Mails\DefaultMail;
use App\UseCases\Client\ClientService;
use App\UseCases\Client\Dtos\ClientDto;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Auth;
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
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  ClientService  $service  クライアントユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(AppRequest $request, ClientService $service): JsonResponse
    {
        $dto = new ClientDto;
        $dto->assign($request->input());

        $value = $service->getClients($dto);

        $response = new IndexResponse;
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

        $response = new ShowResponse;
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

        $value = DB::transaction(function () use ($service, $dto) {
            return $service->store($dto);
        });

        $response = new StoreResponse;
        $response->assign($value->attributes());

        //アクセストークンをメール送信します
        send_mail($value->getEmail(), new DefaultMail($value));

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

        $response = new StoreResponse;
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

        $service->destroy($dto);

        $response = new DestroyResponse;

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
