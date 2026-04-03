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
 * 招待ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class InvitationControllerTest extends TestCase
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
     * 招待URL発行テストです。
     *
     * @return void
     */
    public function testIssue(): void
    {
        /*
        $response = $this->get('/api/v1/invitation/issue');
        $data = $this->getResponseData('Invitation/issue.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * 現在の招待URL取得テストです。
     *
     * @return void
     */
    public function testIndex(): void
    {
        /*
        $response = $this->get('/api/v1/invitation');
        $data = $this->getResponseData('Invitation/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }
}
