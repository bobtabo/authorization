<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use App\Infrastructure\Models\Notification;
use App\Infrastructure\Models\Staff;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Tests\TestCase;

/**
 * 通知ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class NotificationControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * 通知件数集計取得テストです。
     *
     * @return void
     */
    public function testCounts(): void
    {
        $response = $this->withCookies($this->staffCookies(1))
            ->get('/api/notifications/counts');
        $data = $this->getResponseData('Notification/counts.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 通知一覧取得テストです。
     *
     * @return void
     */
    public function testIndex(): void
    {
        $params = $this->getRequestParams('Notification/index.json');
        $response = $this->withCookies($this->staffCookies(1))
            ->get('/api/notifications', $params);
        $data = $this->getResponseData('Notification/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * url付き通知が一覧に含まれるテストです。
     *
     * @return void
     */
    public function testIndexWithUrl(): void
    {
        $staff = \App\Infrastructure\Models\Staff::factory()->create();
        Notification::factory()->create([
            'staff_id' => $staff->id,
            'title'    => 'クライアント登録',
            'url'      => '/clients/show?id=1',
        ]);

        $response = $this->withCookies($this->staffCookies($staff->id))
            ->get('/api/notifications');

        $response
            ->assertStatus(200)
            ->assertJsonPath('items.0.url', '/clients/show?id=1');
    }

    /**
     * 通知トリガー受理テストです。
     *
     * @return void
     */
    public function testStore(): void
    {
        $params = $this->getRequestParams('Notification/store.json');
        $response = $this->post('/api/notifications', $params);
        $data = $this->getResponseData('Notification/store.json');
        $response
            ->assertStatus(202)
            ->assertJson($data);
    }

    /**
     * 通知一括既読テストです。
     *
     * @return void
     */
    public function testBulkPatch(): void
    {
        $response = $this->withCookies($this->staffCookies(1))
            ->patch('/api/notifications');
        $data = $this->getResponseData('Notification/bulkPatch.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 単一通知既読テストです。
     *
     * @return void
     */
    public function testUpdate(): void
    {
        $staff = Staff::factory()->create();
        $notification = Notification::factory()->create(['staff_id' => $staff->id]);
        $id = $notification->id;
        $response = $this->patch("/api/notifications/{$id}");
        $data = $this->getResponseData('Notification/update.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
