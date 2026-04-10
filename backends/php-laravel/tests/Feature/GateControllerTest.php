<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

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
     * TODO: auth:sanctum が本実装されたら Sanctum トークンで認証すること。
     *       現状は middleware をバイパスしてスタブ実装の動作のみ確認する。
     *
     * @return void
     */
    public function testIssue(): void
    {
        $params = $this->getRequestParams('Gate/issue.json');
        $member = $params['member'];
        // TODO: Sanctum 実装後（personal_access_tokens テーブル追加・Bearer トークン発行）は
        //       withoutMiddleware() を削除し、正規の認証ヘッダーを付与すること
        $response = $this->withoutMiddleware()
            ->get("/api/gate/issue?member={$member}");
        $data = $this->getResponseData('Gate/issue.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * JWT検証テストです。
     *
     * TODO: JWT が本実装されたら実際のトークンで検証すること。
     *       現状は StubJwtVerifier の固定レスポンスを確認するのみ。
     *
     * @return void
     */
    public function testVerify(): void
    {
        $params = $this->getRequestParams('Gate/verify.json');
        $token = $params['token'];
        $identifier = 'test-client';
        // TODO: 実装後は正規 JWT を token に渡すこと
        $response = $this->get("/api/gate/client/{$identifier}/verify?token={$token}");
        $data = $this->getResponseData('Gate/verify.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
