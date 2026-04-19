<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Http\Controllers\Api;

use App\Domain\Staff\Enums\Provider;
use App\Http\Controllers\Controller;
use App\Http\Responses\Auth\AuthInvitationResponse;
use App\Http\Responses\Auth\AuthLoginResponse;
use App\Http\Responses\Auth\AuthMeResponse;
use App\Support\Exceptions\AppException;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Auth\AuthService;
use App\UseCases\Auth\Dtos\AuthUserDto;
use App\UseCases\Auth\Dtos\SocialDto;
use App\UseCases\Invitation\Dtos\InvitationDto;
use App\UseCases\Invitation\InvitationService;
use Exception;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Routing\Redirector;
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
     * @param AppRequest $request HTTP リクエスト
     * @param AuthService $service 認証Service
     * @return JsonResponse JSON レスポンス
     */
    public function login(Request $request, AuthService $service): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if ($staffId === null) {
            throw AppException::unauthorized('unauthenticated');
        }

        $dto = new AuthUserDto();
        $dto->id = $staffId;

        $vo = $service->findUser($dto);

        $response = new AuthLoginResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * 招待トークンを検証し、招待情報を返します。
     *
     * @param AppRequest $request HTTP リクエスト
     * @param InvitationService $service 招待Service
     * @return JsonResponse JSON レスポンス
     */
    public function invitation(AppRequest $request, InvitationService $service): JsonResponse
    {
        $dto = new InvitationDto();
        $dto->assign($request->input());
        $dto->token = $request->route('token');

        $vo = $service->findByToken($dto);

        $response = new AuthInvitationResponse();
        $response->assign($vo->attributes());

        return response()->success($response->attributes());
    }

    /**
     * Google へリダイレクトします。
     *
     * @return \Illuminate\Http\RedirectResponse|\Symfony\Component\HttpFoundation\RedirectResponse
     */
    public function googleRedirect(): RedirectResponse|\Symfony\Component\HttpFoundation\RedirectResponse
    {
        return Socialite::driver('google')->stateless()->redirect();
    }

    /**
     * Google からのコールバックを処理します。
     *
     * @param AuthService $service 認証Service
     * @return \Illuminate\Http\RedirectResponse|\Illuminate\Routing\Redirector
     */
    public function googleCallback(AuthService $service): RedirectResponse|Redirector
    {
        $appConfig = config('authorization.app');

        try {
            $googleUser = Socialite::driver('google')->stateless()->user();

            $dto = new SocialDto();
            $dto->assign([
                'provider' => Provider::Google,
                'providerId' => $googleUser->getId(),
                'nickname' => $googleUser->getNickname(),
                'name' => $googleUser->getName(),
                'email' => $googleUser->getEmail(),
                'avatar' => $googleUser->getAvatar(),
            ]);

            $vo = DB::transaction(function () use ($service, $dto) {
                return $service->login($dto);
            });

            $secure = config('app.env') === 'production';
            return redirect($appConfig['frontend_url'] . '/clients')
                ->cookie(
                    'staff_id',
                    (string)$vo->getId(),
                    $appConfig['staff_cookie_lifetime'],
                    '/',
                    null,
                    $secure,
                    true
                );
        } catch (Exception $e) {
            Log::error('googleCallback error: ' . $e->getMessage(), ['exception' => $e]);
            return redirect($appConfig['frontend_url'] . '/error?code=500');
        }
    }

    /**
     * 自分自身のプロフィールを返します（staff_id クッキーで認証済みのユーザー）。
     *
     * @param Request $request HTTP リクエスト
     * @param AuthService $auth 認証Service
     * @return JsonResponse JSON レスポンス
     */
    public function getMyProfile(Request $request, AuthService $auth): JsonResponse
    {
        $staffId = $this->staffIdFromCookie($request);
        if ($staffId === null) {
            throw AppException::unauthorized('unauthenticated');
        }

        $dto = new AuthUserDto();
        $dto->id = $staffId;

        $vo = $auth->findUser($dto);

        $response = new AuthMeResponse();
        $response->assign([
            'staff_id' => $vo->getId(),
            'name' => $vo->getName(),
            'avatar' => $vo->getAvatar(),
            'role' => $vo->getRole(),
        ]);

        return response()->success($response->attributes());
    }

    /**
     * ログアウト処理の応答を返します。
     *
     * @param Request $request HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function logout(Request $request): JsonResponse
    {
        return response()->success()->cookie(\Cookie::forget('staff_id'));
    }
}
