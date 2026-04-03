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
 * スタッフControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class StaffControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * {@inheritdoc}
     */
    protected function setUp(): void
    {
        parent::setUp();

        $testName = $this->toString();
        if (str($testName)->contains('testIndex', true)) {
            //
        }
    }

    /**
     * スタッフ一覧取得テストです。
     *
     * @return void
     */
    public function testIndex(): void
    {
        $params = $this->getRequestParams('Staff/index.json');
        $response = $this->get('/api/staffs', $params);
        $data = $this->getResponseData('Staff/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * スタッフ権限更新テストです。
     *
     * @return void
     */
    public function testUpdateRole(): void
    {
        $params = $this->getRequestParams('Staff/updateRole.json');
        $id = 1;
        $response = $this->patch("/api/staffs/{$id}/updateRole", $params);
        $data = $this->getResponseData('Staff/updateRole.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * スタッフ削除テストです。
     *
     * @return void
     */
    public function testDestroy(): void
    {
        $params = $this->getRequestParams('Staff/destroy.json');
        $id = $params['id'];
        $response = $this->delete("/api/staffs/{$id}/delete");
        $data = $this->getResponseData('Staff/destroy.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
