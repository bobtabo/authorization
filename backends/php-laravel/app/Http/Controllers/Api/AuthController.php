<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Domain\Staff\Enums\Provider;
use App\Http\Controllers\Controller;
use App\Http\Responses\Auth\AuthInvitationResponse;
use App\Http\Responses\Auth\AuthLoginResponse;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Auth\AuthService;
use App\UseCases\Auth\Dtos\AuthUserDto;
use App\UseCases\Auth\Dtos\SocialDto;
use App\UseCases\Invitation\Dtos\InvitationDto;
use App\UseCases\Invitation\InvitationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Laravel\Socialite\Facades\Socialite;

/**
 * 認証Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Controllers\Api
 */
class AuthController extends Controller
{
    /**
     * ログイン情報を返します（セッション／トークンで認証済みのユーザー）。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  AuthService  $auth  認証ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function login(AppRequest $request, AuthService $auth): JsonResponse
    {
        $dto = new AuthUserDto;
        $dto->assign($request->input());

        $vo = $auth->findUser($dto);
        if (empty($vo->getId())) {
            return response()->json(['message' => 'ユーザーが存在しません。'], 404);
        }

        $response = new AuthLoginResponse;
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }

    /**
     * 招待トークンを検証し、招待情報を返します。
     *
     * @param  AppRequest  $request  HTTP リクエスト
     * @param  InvitationService  $invitations  招待ユースケース
     * @return JsonResponse JSON レスポンス
     */
    public function invitation(AppRequest $request, InvitationService $invitations): JsonResponse
    {
        $dto = new InvitationDto;
        $dto->assign($request->input());
        $dto->token = $request->route('token');

        $vo = $invitations->findByToken($dto);
        if (! $vo->isFound()) {
            return response()->json(['message' => '招待が無効です。'], 404);
        }

        $response = new AuthInvitationResponse;
        $response->assign($vo->attributes());

        return response()->json($response->attributes());
    }

    /**
     * Google へリダイレクトします。
     *
     * @return \Illuminate\Http\RedirectResponse|\Symfony\Component\HttpFoundation\RedirectResponse
     */
    public function googleRedirect(): \Illuminate\Http\RedirectResponse|\Symfony\Component\HttpFoundation\RedirectResponse
    {
        return Socialite::driver('google')->stateless()->redirect();
    }

    /**
     * Google からのコールバックを処理します。
     *
     * @param  AuthService  $auth  認証ユースケース
     * @return \Illuminate\Http\RedirectResponse|\Illuminate\Routing\Redirector
     */
    public function googleCallback(AuthService $service): \Illuminate\Http\RedirectResponse|\Illuminate\Routing\Redirector
    {
        try {
            $googleUser = Socialite::driver('google')->stateless()->user();

            $dto = new SocialDto;
            $dto->assign([
                'provider' => Provider::Google,
                'providerId' => $googleUser->getId(),
                'nickname' => $googleUser->getNickname(),
                'name' => $googleUser->getName(),
                'email' => $googleUser->getEmail(),
                'avatar' => $googleUser->getAvatar(),
            ]);

            DB::transaction(function () use ($service, $dto) {
                $service->login($dto);
            });

            return redirect(config('authorization.app.frontend_url') . '/clients');
        } catch (\Exception $e) {
            \Log::error('googleCallback error: ' . $e->getMessage(), ['exception' => $e]);
            return redirect(config('authorization.app.frontend_url') . '/login');
        }
    }

    /**
     * ログアウト処理の応答を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function logout(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'SUCCESS',
        ]);
    }
}
