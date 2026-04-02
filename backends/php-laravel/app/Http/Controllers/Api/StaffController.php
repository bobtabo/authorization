<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Domain\Staff\Enums\StaffRole;
use App\Http\Controllers\Controller;
use App\UseCases\Staff\Dtos\StaffDto;
use App\UseCases\Staff\StaffService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

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
     * @param  Request  $request  HTTP リクエスト
     * @param  StaffService  $staff  スタッフユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request, StaffService $staff): JsonResponse
    {
        $keyword = $request->query('keyword');
        $keyword = is_string($keyword) && $keyword !== '' ? $keyword : null;

        $roles = $this->intListFromQuery($request->query('roles'));
        $statuses = $this->intListFromQuery($request->query('statuses'));

        $dto = new StaffDto;
        $dto->keyword = $keyword;
        $dto->roles = $roles;
        $dto->statuses = $statuses;

        $vo = $staff->index($dto);

        return response()->json($vo->attributes()['items']);
    }

    /**
     * スタッフの権限を更新します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  StaffService  $staff  スタッフユースケース
     * @param  int  $id  スタッフID
     * @return JsonResponse JSON レスポンス
     */
    public function updateRole(Request $request, StaffService $staff, int $id): JsonResponse
    {
        $validated = $request->validate([
            'role' => 'required|integer',
        ]);

        $role = StaffRole::tryFrom((int) $validated['role']);
        if ($role === null) {
            return response()->json(['message' => '権限の指定が不正です。'], 400);
        }

        $dto = new StaffDto;
        $dto->id = $id;
        $dto->role = $role->value;
        $dto->executorId = $this->executorId();

        $vo = $staff->updateRole($dto);
        if (! $vo->ok) {
            return response()->json(['message' => 'スタッフが存在しません。'], 404);
        }

        return response()->json([
            'message' => $vo->message,
            'id' => $vo->id,
        ]);
    }

    /**
     * スタッフを論理削除します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  StaffService  $staff  スタッフユースケース
     * @param  int  $id  スタッフID
     * @return JsonResponse JSON レスポンス
     */
    public function destroy(Request $request, StaffService $staff, int $id): JsonResponse
    {
        $dto = new StaffDto;
        $dto->id = $id;
        $dto->executorId = $this->executorId();

        $vo = $staff->destroy($dto);
        if (! $vo->ok) {
            return response()->json(['message' => 'スタッフが存在しません。'], 404);
        }

        return response()->json([
            'message' => $vo->message,
            'id' => $vo->id,
        ]);
    }

    /**
     * クエリの単一値または配列を int のリストにします。
     *
     * @param  array<int|string>|string|int|null  $raw
     * @return array<int, int>
     */
    private function intListFromQuery(array|string|int|null $raw): array
    {
        if ($raw === null || $raw === '' || $raw === []) {
            return [];
        }
        $list = is_array($raw) ? $raw : [$raw];
        $out = [];
        foreach ($list as $v) {
            if ($v === '' || $v === null) {
                continue;
            }
            $out[] = (int) $v;
        }

        return $out;
    }

    /**
     * @return int|null 未ログイン等のときは null
     */
    private function executorId(): ?int
    {
        $id = Auth::id();

        return $id !== null ? (int) $id : null;
    }
}
