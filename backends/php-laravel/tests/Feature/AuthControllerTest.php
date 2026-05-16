<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use App\Domain\Invitation\Repositories\InvitationAuthRepository;
use App\Infrastructure\Models\Invitation;
use App\Infrastructure\Models\Staff;
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
     * ログイン情報取得テストです。
     *
     * @return void
     */
    public function testLogin(): void
    {
        $staff = Staff::factory()->create();
        $response = $this->withStaffCookie($staff->id)
            ->get('/api/auth/login');
        $response
            ->assertStatus(200)
            ->assertJsonStructure(['id', 'name', 'email', 'avatar']);
    }

    /**
     * 招待トークン検証テストです。
     *
     * @return void
     */
    public function testInvitation(): void
    {
        $invitation = Invitation::factory()->create([
            'token' => 'dummy-token',
        ]);
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
        $this->markTestSkipped('Requires valid Google OAuth config');
    }

    /**
     * Google OAuth コールバック（既存ユーザー）テストです。
     * 招待トークン不要でログインできることを確認します。
     *
     * @return void
     */
    public function testGoogleCallback(): void
    {
        Staff::factory()->create([
            'provider'    => 1,
            'provider_id' => '123456789',
        ]);

        $abstractUser = Mockery::mock('Laravel\Socialite\Two\User');
        $abstractUser->shouldReceive('getId')->andReturn('123456789');
        $abstractUser->shouldReceive('getEmail')->andReturn('nagashiba@example.com');
        $abstractUser->shouldReceive('getName')->andReturn('長柴 監督');
        $abstractUser->shouldReceive('getNickname')->andReturn('nagashiba');
        $abstractUser->shouldReceive('getAvatar')->andReturn('https://example.com/photo.jpg');

        $mockProvider = Mockery::mock();
        $mockProvider->shouldReceive('stateless')->andReturnSelf();
        $mockProvider->shouldReceive('user')->andReturn($abstractUser);
        Socialite::shouldReceive('driver')->with('google')->andReturn($mockProvider);

        $response = $this->get('/auth/google/callback');

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl . '/clients');
    }

    /**
     * Google OAuth コールバック（新規ユーザー・招待トークンあり）テストです。
     * 有効な招待トークンがある場合に新規登録できることを確認します。
     *
     * @return void
     */
    public function testGoogleCallbackNewUserWithInvitation(): void
    {
        $token = 'valid-invitation-token';
        $this->app->make(InvitationAuthRepository::class)->store($token, 2, 600);

        $abstractUser = Mockery::mock('Laravel\Socialite\Two\User');
        $abstractUser->shouldReceive('getId')->andReturn('new-user-999');
        $abstractUser->shouldReceive('getEmail')->andReturn('newuser@example.com');
        $abstractUser->shouldReceive('getName')->andReturn('新規 ユーザー');
        $abstractUser->shouldReceive('getNickname')->andReturn('newuser');
        $abstractUser->shouldReceive('getAvatar')->andReturn(null);

        $mockProvider = Mockery::mock();
        $mockProvider->shouldReceive('stateless')->andReturnSelf();
        $mockProvider->shouldReceive('user')->andReturn($abstractUser);
        Socialite::shouldReceive('driver')->with('google')->andReturn($mockProvider);

        $response = $this->get('/auth/google/callback?state=' . $token);

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl . '/clients');
        $this->assertNull($this->app->make(InvitationAuthRepository::class)->find($token));
    }

    /**
     * Google OAuth コールバック（新規ユーザー・招待トークンなし）テストです。
     * 招待トークンなしで新規登録しようとした場合に 403 エラーページへリダイレクトすることを確認します。
     *
     * @return void
     */
    public function testGoogleCallbackNewUserWithoutInvitation(): void
    {
        $abstractUser = Mockery::mock('Laravel\Socialite\Two\User');
        $abstractUser->shouldReceive('getId')->andReturn('new-user-888');
        $abstractUser->shouldReceive('getEmail')->andReturn('noninvited@example.com');
        $abstractUser->shouldReceive('getName')->andReturn('招待なし ユーザー');
        $abstractUser->shouldReceive('getNickname')->andReturn('noninvited');
        $abstractUser->shouldReceive('getAvatar')->andReturn(null);

        $mockProvider = Mockery::mock();
        $mockProvider->shouldReceive('stateless')->andReturnSelf();
        $mockProvider->shouldReceive('user')->andReturn($abstractUser);
        Socialite::shouldReceive('driver')->with('google')->andReturn($mockProvider);

        $response = $this->get('/auth/google/callback');

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl . '/error?code=403');
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
