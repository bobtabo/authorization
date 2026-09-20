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
     */
    public function test_counts(): void
    {
        $response = $this->withStaffCookie(1)
            ->get('/api/notifications/counts');
        $data = $this->getResponseData('Notification/counts.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 通知一覧取得テストです。
     */
    public function test_index(): void
    {
        $params = $this->getRequestParams('Notification/index.json');
        $response = $this->withStaffCookie(1)
            ->get('/api/notifications', $params);
        $data = $this->getResponseData('Notification/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * url付き通知が一覧に含まれるテストです。
     */
    public function test_index_with_url(): void
    {
        $staff = Staff::factory()->create();
        Notification::factory()->create([
            'staff_id' => $staff->id,
            'title' => 'クライアント登録',
            'url' => '/clients/show?id=1',
        ]);

        $response = $this->withStaffCookie($staff->id)
            ->get('/api/notifications');

        $response
            ->assertStatus(200)
            ->assertJsonPath('items.0.url', '/clients/show?id=1');
    }

    /**
     * 通知一括既読テストです。
     */
    public function test_bulk_patch(): void
    {
        $response = $this->withStaffCookie(1)
            ->patch('/api/notifications');
        $data = $this->getResponseData('Notification/bulkPatch.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 単一通知既読テストです。
     */
    public function test_update(): void
    {
        $staff = Staff::factory()->create();
        $notification = Notification::factory()->create(['staff_id' => $staff->id]);
        $id = $notification->id;
        $response = $this->withStaffCookie($staff->id)
            ->patch("/api/notifications/{$id}");
        $data = $this->getResponseData('Notification/update.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    public function test_update_already_read(): void
    {
        $staff = Staff::factory()->create();
        $notification = Notification::factory()->create([
            'staff_id' => $staff->id,
            'read' => true,
        ]);
        $response = $this->withStaffCookie($staff->id)
            ->patch("/api/notifications/{$notification->id}");
        $response->assertStatus(200);
    }

    /**
     * 単一通知既読の未認証テストです。
     */
    public function test_update_unauthenticated(): void
    {
        $staff = Staff::factory()->create();
        $notification = Notification::factory()->create(['staff_id' => $staff->id]);
        $response = $this->patch("/api/notifications/{$notification->id}");
        $response->assertStatus(401);
    }

    /**
     * 他staffの通知を既読にしようとすると404になるテストです。
     */
    public function test_update_other_staff_notification_not_found(): void
    {
        $staff = Staff::factory()->create();
        $other = Staff::factory()->create();
        $notification = Notification::factory()->create(['staff_id' => $other->id]);
        $response = $this->withStaffCookie($staff->id)
            ->patch("/api/notifications/{$notification->id}");
        $response->assertStatus(404);
    }
}
