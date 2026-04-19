<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

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
     * Google OAuth コールバックテストです。
     *
     * @return void
     */
    public function testGoogleCallback(): void
    {
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
