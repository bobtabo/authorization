<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use App\Infrastructure\Models\Staff;
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
        $response = $this->withHeader('X-Executor-Id', '1')
            ->patch("/api/staffs/{$id}/updateRole", $params);
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
        $staff = Staff::factory()->create();
        $id = $staff->id;
        $response = $this->withHeader('X-Executor-Id', '1')
            ->delete("/api/staffs/{$id}/delete");
        $data = $this->getResponseData('Staff/destroy.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
