<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Http\Requests\AppRequest;
use App\UseCases\Auth\AuthService;
use App\UseCases\Auth\Dtos\AuthUserDto;
use app\UseCases\Auth\Dtos\SocialDto;
use App\UseCases\Invitation\Dtos\InvitationDto;
use App\UseCases\Invitation\InvitationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
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
        $userId = Auth::id();
        if ($userId === null) {
            return response()->json(['message' => '認証されていません。'], 401);
        }

        $dto = new AuthUserDto;
        $dto->id = (int) $userId;
        $vo = $auth->findUser($dto);
        if (! $vo->found) {
            return response()->json(['message' => 'ユーザーが存在しません。'], 404);
        }

        return response()->json([
            'id' => $vo->id,
            'name' => $vo->name,
            'email' => $vo->email,
        ]);
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
        if (!$vo->isFound()) {
            return response()->json(['message' => '招待が無効です。'], 404);
        }

        return response()->json([
            'url' => $vo->url,
            'token' => $vo->token,
        ]);
    }

    /**
     * Googleへリダイレクトします。
     *
     * @return \Illuminate\Http\RedirectResponse|\Symfony\Component\HttpFoundation\RedirectResponse
     */
    public function redirectToGoogle() {
        return Socialite::driver('google')->redirect();
    }

    /**
     * Googleからのコールバックを処理します。
     *
     * @return \Illuminate\Http\RedirectResponse|\Illuminate\Routing\Redirector
     */
    public function handleGoogleCallback() {
        try {
            $googleUser = Socialite::driver('google')->user();

            $dto = new SocialDto;
            $dto->assign([
                'id' => $googleUser->id,
                'nickname' => $googleUser->nickname,
                'name' => $googleUser->name,
                'email' => $googleUser->email,
                'avatar' => $googleUser->avatar,
            ]);

            // ユーザーを探す、なければ作る（FirstOrCreate）
            $user = User::updateOrCreate([
                'google_id' => $googleUser->id,
            ], [
                'name' => $googleUser->name,
                'email' => $googleUser->email,
                // パスワードは適当なランダム文字列か、null許容にする
            ]);

            Auth::login($user);

            return redirect('/dashboard');
        } catch (\Exception $e) {
            return redirect('/login')->with('error', 'Google認証に失敗しました');
        }
    }

    /**
     * Google OAuth リダイレクト用の応答を返します（疎通用スタブ）。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function googleRedirect(Request $request): JsonResponse
    {
        return response()->json([
            'message' => 'Google OAuth リダイレクトは未接続です。',
            'authorization_url' => null,
        ]);
    }

    /**
     * ログアウト処理の応答を返します。
     *
     * @param  Request  $request  HTTP リクエスト
     * @return JsonResponse JSON レスポンス
     */
    public function logout(Request $request): JsonResponse
    {
        Auth::logout();

        return response()->json([
            'message' => 'SUCCESS',
        ]);
    }
}
