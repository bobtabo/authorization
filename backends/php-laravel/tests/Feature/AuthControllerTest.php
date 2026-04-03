<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Tests\Feature;

use Illuminate\Foundation\Testing\DatabaseMigrations;
use Laravel\Socialite\Facades\Socialite;
use Mockery;
use Tests\TestCase;

/**
 * 認証ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class AuthControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * {@inheritdoc}
     */
    protected function setUp(): void
    {
        parent::setUp();

        $testName = $this->toString();
        if (str($testName)->contains('testLogin', true)) {
            //
        }
    }

    /**
     * ログイン情報取得テストです。
     *
     * @return void
     */
    public function testLogin(): void
    {
        $params = $this->getRequestParams('Auth/login.json');
        $response = $this->get('/api/auth/login', $params);
        $data = $this->getResponseData('Auth/login.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 招待トークン検証テストです。
     *
     * @return void
     */
    public function testInvitation(): void
    {
        $params = $this->getRequestParams('Auth/invitation.json');
        $token = $params['token'];
        $response = $this->get("/api/auth/invitation/{$token}");
        $data = $this->getResponseData('Auth/invitation.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * Google OAuth リダイレクトテストです。
     *
     * @return void
     */
    public function testGoogleRedirect(): void
    {
//        $response = $this->get('/api/auth/google/redirect');
//        $data = $this->getResponseData('Auth/googleRedirect.json');
//        $response
//            ->assertStatus(200)
//            ->assertJson($data);

        // 実際にGoogleへ飛ばないように「フェイク」を仕掛ける
        $response = $this->get('/auth/google/redirect');

        // 302リダイレクトが発生し、宛先がGoogleであることを確認
        $response->assertStatus(302);
        $this->assertStringContainsString('accounts.google.com', $response->getTargetUrl());
    }

    /**
     * Google OAuth コールバックテストです。
     *
     * @return void
     */
    public function testGoogleCallback(): void
    {
//        $response = $this->get('/api/auth/google/callback');
//        $data = $this->getResponseData('Auth/googleCallback.json');
//        $response
//            ->assertStatus(200)
//            ->assertJson($data);

        // 1. Googleから返ってくる「偽のユーザー」を定義
        $abstractUser = Mockery::mock('Laravel\Socialite\Two\User');
        $abstractUser->shouldReceive('getId')->andReturn('123456789');
        $abstractUser->shouldReceive('getEmail')->andReturn('nagashiba@example.com');
        $abstractUser->shouldReceive('getName')->andReturn('長柴 監督');
        $abstractUser->shouldReceive('getAvatar')->andReturn('https://example.com/photo.jpg');

        // 2. SocialiteがGoogleからこのユーザーを受け取った「フリ」をさせる
        Socialite::shouldReceive('driver')->with('google')->andReturn(Mockery::self());
        Socialite::shouldReceive('user')->andReturn($abstractUser);

        // 3. コールバックURLを叩く
        $response = $this->get('/auth/google/callback');

        // 4. 正しくログインされ、ダッシュボードへリダイレクトされるか確認
        $response->assertRedirect('/dashboard');
        $this->assertAuthenticated(); // ログイン状態であることを検証
    }

    /**
     * ログアウトテストです。
     *
     * @return void
     */
    public function testLogout(): void
    {
        $response = $this->get('/api/auth/logout');
        $data = $this->getResponseData('Auth/logout.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
