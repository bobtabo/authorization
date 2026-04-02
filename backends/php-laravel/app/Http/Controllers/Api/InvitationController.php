<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\UseCases\Invitation\Dtos\InvitationDto;
use App\UseCases\Invitation\InvitationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/**
 * 招待Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class InvitationController extends Controller
{
    /**
     * 現在の招待 URL を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  InvitationService  $invitations  招待ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function index(Request $request, InvitationService $invitations): JsonResponse
    {
        $vo = $invitations->current(new InvitationDto);
        if (! $vo->found) {
            return response()->json(['message' => '招待情報がありません。'], 404);
        }

        return response()->json([
            'url' => $vo->url,
            'token' => $vo->token,
        ]);
    }

    /**
     * 招待 URL を発行します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @param  InvitationService  $invitations  招待ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function issue(Request $request, InvitationService $invitations): JsonResponse
    {
        $vo = $invitations->issue(new InvitationDto);

        return response()->json([
            'url' => $vo->url,
            'token' => $vo->token,
        ]);
    }
}
