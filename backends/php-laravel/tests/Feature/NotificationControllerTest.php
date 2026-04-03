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
 * 通知ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class NotificationControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * {@inheritdoc}
     */
    protected function setUp(): void
    {
        parent::setUp();

        $testName = $this->toString();
        if (str($testName)->contains('testCounts', true)) {
            //
        }
    }

    /**
     * 通知件数集計取得テストです。
     *
     * @return void
     */
    public function testCounts(): void
    {
        $response = $this->get('/api/notifications/counts');
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
        $response = $this->get('/api/notifications', $params);
        $data = $this->getResponseData('Notification/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
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
     * 通知一括更新テストです。
     *
     * @return void
     */
    public function testBulkPatch(): void
    {
        $params = $this->getRequestParams('Notification/bulkPatch.json');
        $response = $this->patch('/api/notifications', $params);
        $data = $this->getResponseData('Notification/bulkPatch.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 単一通知更新テストです。
     *
     * @return void
     */
    public function testUpdate(): void
    {
        $params = $this->getRequestParams('Notification/update.json');
        $id = '00000000-0000-0000-0000-000000000001';
        $response = $this->patch("/api/notifications/{$id}", $params);
        $data = $this->getResponseData('Notification/update.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
