<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use App\Infrastructure\Models\Client;
use App\Infrastructure\Models\Staff;
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
     * クライアント一覧取得テストです。
     *
     * @return void
     */
    public function testIndex(): void
    {
        $params = $this->getRequestParams('Client/index.json');
        $response = $this->get('/api/clients', $params);
        $data = $this->getResponseData('Client/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * クライアント詳細取得テストです。
     *
     * @return void
     */
    public function testShow(): void
    {
        $params = $this->getRequestParams('Client/show.json');
        $id = $params['id'];
        $response = $this->get("/api/clients/{$id}");
        $data = $this->getResponseData('Client/show.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * クライアント登録テストです。
     *
     * @return void
     */
    public function testStore(): void
    {
        $params = $this->getRequestParams('Client/store.json');
        $response = $this->post('/api/clients/store', $params);
        $data = $this->getResponseData('Client/store.json');
        $response
            ->assertStatus(201)
            ->assertJson($data);
    }

    /**
     * クライアント更新テストです。
     *
     * @return void
     */
    public function testUpdate(): void
    {
        $client = Client::factory()->create();
        $params = $this->getRequestParams('Client/update.json');
        $id = $client->id;
        $response = $this->put("/api/clients/{$id}/update", array_merge($params, ['id' => $id]));
        $data = $this->getResponseData('Client/update.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * クライアント削除テストです。
     *
     * @return void
     */
    public function testDestroy(): void
    {
        $staff = Staff::factory()->create();
        $client = Client::factory()->create();
        $id = $client->id;
        $response = $this->withStaffCookie($staff->id)
            ->delete("/api/clients/{$id}/delete");
        $data = $this->getResponseData('Client/destroy.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
