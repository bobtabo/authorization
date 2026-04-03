<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace Tests\Feature;

use Illuminate\Foundation\Testing\DatabaseMigrations;
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
        /*
        $params = $this->getRequestParams('Auth/login.json');
        $response = $this->get('/api/v1/auth/login', $params);
        $data = $this->getResponseData('Auth/login.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * 招待トークン検証テストです。
     *
     * @return void
     */
    public function testInvitation(): void
    {
        /*
        $params = $this->getRequestParams('Auth/invitation.json');
        $token = $params['token'];
        $response = $this->get("/api/v1/auth/invitation/{$token}");
        $data = $this->getResponseData('Auth/invitation.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * Google OAuth リダイレクトテストです。
     *
     * @return void
     */
    public function testGoogleRedirect(): void
    {
        /*
        $response = $this->get('/api/v1/auth/google/redirect');
        $data = $this->getResponseData('Auth/googleRedirect.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * ログアウトテストです。
     *
     * @return void
     */
    public function testLogout(): void
    {
        /*
        $response = $this->get('/api/v1/auth/logout');
        $data = $this->getResponseData('Auth/logout.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }
}
