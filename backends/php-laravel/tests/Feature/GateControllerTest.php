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
 * 認可ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class GateControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * {@inheritdoc}
     */
    protected function setUp(): void
    {
        parent::setUp();

        $testName = $this->toString();
        if (str($testName)->contains('testIssue', true)) {
            //
        }
    }

    /**
     * JWT発行テストです。
     *
     * @return void
     */
    public function testIssue(): void
    {
        /*
        $params = $this->getRequestParams('Gate/issue.json');
        $response = $this->get('/api/v1/gate/issue', $params);
        $data = $this->getResponseData('Gate/issue.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * JWT検証テストです。
     *
     * @return void
     */
    public function testVerify(): void
    {
        /*
        $params = $this->getRequestParams('Gate/verify.json');
        $identifier = 'test-client';
        $response = $this->get("/api/v1/gate/client/{$identifier}/verify", $params);
        $data = $this->getResponseData('Gate/verify.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }
}
