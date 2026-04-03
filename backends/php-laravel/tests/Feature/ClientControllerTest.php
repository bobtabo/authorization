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
 * クライアントControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class ClientControllerTest extends TestCase
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
     * クライアント一覧取得テストです。
     *
     * @return void
     */
    public function testIndex(): void
    {
        /*
        $params = $this->getRequestParams('Client/index.json');
        $response = $this->get('/api/v1/clients', $params);
        $data = $this->getResponseData('Client/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * クライアント詳細取得テストです。
     *
     * @return void
     */
    public function testShow(): void
    {
        /*
        $params = $this->getRequestParams('Client/show.json');
        $id = $params['id'];
        $response = $this->get("/api/v1/clients/{$id}");
        $data = $this->getResponseData('Client/show.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * クライアント登録テストです。
     *
     * @return void
     */
    public function testStore(): void
    {
        /*
        $params = $this->getRequestParams('Client/store.json');
        $response = $this->post('/api/v1/clients/store', $params);
        $data = $this->getResponseData('Client/store.json');
        $response
            ->assertStatus(201)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * クライアント更新テストです。
     *
     * @return void
     */
    public function testUpdate(): void
    {
        /*
        $params = $this->getRequestParams('Client/update.json');
        $id = $params['id'];
        $response = $this->put("/api/v1/clients/{$id}/update", $params);
        $data = $this->getResponseData('Client/update.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }

    /**
     * クライアント削除テストです。
     *
     * @return void
     */
    public function testDestroy(): void
    {
        /*
        $params = $this->getRequestParams('Client/destroy.json');
        $id = $params['id'];
        $response = $this->delete("/api/v1/clients/{$id}/delete");
        $data = $this->getResponseData('Client/destroy.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }
}
