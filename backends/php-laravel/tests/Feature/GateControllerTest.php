<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use App\Infrastructure\Models\Client;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Tests\TestCase;

/**
 * 認可ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class GateControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * JWT発行テストです。
     *
     * @return void
     */
    public function testIssue(): void
    {
        $client = Client::factory()->create(['status' => 2]);

        $response = $this->withToken($client->access_token)
            ->get('/api/gate/issue?member=member-001');

        $response
            ->assertStatus(200)
            ->assertJsonStructure(['message', 'token']);
    }

    /**
     * JWT発行 利用中以外のクライアントテストです。
     *
     * @return void
     */
    public function testIssueWithNonActiveClient(): void
    {
        $client = Client::factory()->create(['status' => 3]);

        $response = $this->withoutMiddleware()
            ->withToken($client->access_token)
            ->get('/api/gate/issue?member=member-001');

        $response
            ->assertStatus(401)
            ->assertJson(['message' => 'クライアントが存在しません。']);
    }

    /**
     * JWT検証テストです。
     *
     * @return void
     */
    public function testVerify(): void
    {
        $client = Client::factory()->create(['status' => 2]);
        $identifier = $client->identifier;

        // issue で JWT を取得（キャッシュキーがテスト実行ごとに一意になるようランダム identifier を使用）
        $issueResponse = $this->withoutMiddleware()
            ->withToken($client->access_token)
            ->get("/api/gate/issue?member=member-001");
        $jwt = $issueResponse->json('token');

        // verify で Payload を確認
        $response = $this->get("/api/gate/client/{$identifier}/verify?token={$jwt}");
        $response
            ->assertStatus(200)
            ->assertJson([
                'message' => 'SUCCESS',
                'iss' => 'authorization',
                'aud' => $identifier,
                'sub' => 'member-001',
            ]);
    }

    /**
     * JWT発行 member パラメーター未指定テストです。
     *
     * @return void
     */
    public function testIssueWithoutMember(): void
    {
        $response = $this->withoutMiddleware()
            ->get('/api/gate/issue');
        $response
            ->assertStatus(400)
            ->assertJson(['message' => 'member を指定してください。']);
    }

    /**
     * JWT発行 アクセストークン不一致テストです（クライアント未検出）。
     *
     * @return void
     */
    public function testIssueWithInvalidToken(): void
    {
        $response = $this->withoutMiddleware()
            ->withToken('invalid-token')
            ->get('/api/gate/issue?member=member-001');
        $response
            ->assertStatus(401)
            ->assertJson(['message' => 'クライアントが存在しません。']);
    }

    /**
     * JWT検証 token パラメーター未指定テストです。
     *
     * @return void
     */
    public function testVerifyWithoutToken(): void
    {
        $response = $this->get('/api/gate/client/test-client/verify');
        $response
            ->assertStatus(400)
            ->assertJson(['message' => 'token を指定してください。']);
    }

    /**
     * JWT検証 クライアント識別名不一致テストです。
     *
     * @return void
     */
    public function testVerifyWithUnknownIdentifier(): void
    {
        $response = $this->get('/api/gate/client/unknown-client/verify?token=dummy');
        $response
            ->assertStatus(403)
            ->assertJson(['message' => 'クライアントが存在しません。']);
    }

    /**
     * JWT検証 無効トークンテストです。
     *
     * @return void
     */
    public function testVerifyWithInvalidJwt(): void
    {
        Client::factory()->create(['identifier' => 'test-client', 'status' => 2]);

        $response = $this->get('/api/gate/client/test-client/verify?token=invalid.jwt.token');
        $response
            ->assertStatus(401)
            ->assertJson(['message' => 'JWT が無効です。']);
    }
}
