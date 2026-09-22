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
use App\Support\Http\StaffSession;
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
     */
    public function test_login(): void
    {
        $staff = Staff::factory()->create();
        $response = $this->withStaffCookie($staff->id)
            ->get('/api/auth/login');
        $response
            ->assertStatus(200)
            ->assertJsonStructure(['id', 'name', 'email', 'avatar']);
    }

    /**
     * 未認証でログイン情報を取得すると401が返ることのテストです。
     */
    public function test_login_unauthenticated_returns401(): void
    {
        $response = $this->get('/api/auth/login');
        $response->assertStatus(401);
    }

    /**
     * 署名の無い偽造クッキーでは401が返ることのテストです。
     */
    public function test_login_unsigned_forged_cookie_returns401(): void
    {
        $staff = Staff::factory()->create();
        $response = $this->withUnencryptedCookies(['staff_id' => (string) $staff->id])
            ->get('/api/auth/login');
        $response->assertStatus(401);
    }

    /**
     * 署名が不正なクッキーでは401が返ることのテストです。
     */
    public function test_login_tampered_signature_returns401(): void
    {
        $staff = Staff::factory()->create();
        $signed = StaffSession::sign($staff->id, config('authorization.app.staff_cookie_secret'), 3600);
        // 末尾の1文字を必ず異なる値に置き換える（元の値と偶然一致すると署名が
        // 変わらずテストが不安定になるため）。
        $replacement = $signed[-1] === '0' ? '1' : '0';
        $tampered = substr($signed, 0, -1).$replacement;
        $response = $this->withUnencryptedCookies(['staff_id' => $tampered])
            ->get('/api/auth/login');
        $response->assertStatus(401);
    }

    /**
     * 別のシークレットで署名されたクッキーでは401が返ることのテストです。
     */
    public function test_login_signed_with_another_secret_returns401(): void
    {
        $staff = Staff::factory()->create();
        $forged = StaffSession::sign($staff->id, 'attacker-controlled-secret', 3600);
        $response = $this->withUnencryptedCookies(['staff_id' => $forged])
            ->get('/api/auth/login');
        $response->assertStatus(401);
    }

    /**
     * 有効期限切れの署名済みクッキーでは401が返ることのテストです。
     * 署名自体は正しいが、Max-Ageが切れた後に手動でCookieヘッダーを再送した状況を
     * 再現する（署名対象に有効期限を含めていないと防げない）。
     */
    public function test_login_expired_signed_cookie_returns401(): void
    {
        $staff = Staff::factory()->create();
        $expired = StaffSession::sign($staff->id, config('authorization.app.staff_cookie_secret'), -3600);
        $response = $this->withUnencryptedCookies(['staff_id' => $expired])
            ->get('/api/auth/login');
        $response->assertStatus(401);
    }

    /**
     * 招待トークン検証テストです。
     */
    public function test_invitation(): void
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
     */
    public function test_google_redirect(): void
    {
        $this->markTestSkipped('Requires valid Google OAuth config');
    }

    /**
     * Google OAuth コールバック（既存ユーザー）テストです。
     * 招待トークン不要でログインできることを確認します。
     */
    public function test_google_callback(): void
    {
        Staff::factory()->create([
            'provider' => 1,
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

        $response = $this->withUnencryptedCookie('oauth_state', 'nonce-1')
            ->get('/auth/google/callback?state=php%7Cnonce-1');

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl.'/clients');
        $response->assertCookieExpired('oauth_state');
    }

    /**
     * Google OAuth コールバック（新規ユーザー・招待トークンあり）テストです。
     * 有効な招待トークンがある場合に新規登録できることを確認します。
     */
    public function test_google_callback_new_user_with_invitation(): void
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

        $response = $this->withUnencryptedCookie('oauth_state', 'nonce-1')
            ->get('/auth/google/callback?state=php%7Cnonce-1%7C'.$token);

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl.'/clients');
        $this->assertNull($this->app->make(InvitationAuthRepository::class)->find($token));
    }

    /**
     * Google OAuth コールバック（新規ユーザー・招待トークンなし）テストです。
     * 招待トークンなしで新規登録しようとした場合に 403 エラーページへリダイレクトすることを確認します。
     */
    public function test_google_callback_new_user_without_invitation(): void
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

        $response = $this->withUnencryptedCookie('oauth_state', 'nonce-1')
            ->get('/auth/google/callback?state=php%7Cnonce-1');

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl.'/error?code=403');
    }

    /**
     * Google OAuth リダイレクトテストです。
     * nonce クッキーを発行し、state に "{runtime}|{nonce}|{token}" を埋め込むことを確認します。
     */
    public function test_google_redirect_issues_o_auth_state_nonce(): void
    {
        $response = $this->get('/auth/google/redirect?token=inv-token');

        $response->assertStatus(302);
        $cookie = $response->getCookie('oauth_state', false);
        $this->assertNotNull($cookie);
        $this->assertTrue($cookie->isHttpOnly());
        $nonce = $cookie->getValue();
        $this->assertNotSame('', $nonce);

        parse_str((string) parse_url($response->headers->get('Location'), PHP_URL_QUERY), $query);
        $runtime = config('authorization.app.runtime');
        $this->assertSame("{$runtime}|{$nonce}|inv-token", $query['state']);
    }

    /**
     * nonce クッキーが無いコールバックは 400 エラーページへリダイレクトすることを確認します。
     */
    public function test_google_callback_without_nonce_cookie(): void
    {
        $response = $this->get('/auth/google/callback?state=php%7Cnonce-1');

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl.'/error?code=400');
    }

    /**
     * nonce が一致しないコールバックは 400 エラーページへリダイレクトすることを確認します。
     */
    public function test_github_callback_with_mismatched_nonce(): void
    {
        $response = $this->withUnencryptedCookie('oauth_state', 'nonce-1')
            ->get('/auth/github/callback?state=php%7Cother');

        $frontendUrl = config('authorization.app.frontend_url');
        $response->assertRedirect($frontendUrl.'/error?code=400');
    }

    /**
     * ログアウトテストです。
     */
    public function test_logout(): void
    {
        $response = $this->get('/api/auth/logout');
        $data = $this->getResponseData('Auth/logout.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
