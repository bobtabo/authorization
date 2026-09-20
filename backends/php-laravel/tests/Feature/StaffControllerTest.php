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
     */
    public function test_index(): void
    {
        $params = $this->getRequestParams('Staff/index.json');
        $response = $this->get('/api/staffs', $params);
        $data = $this->getResponseData('Staff/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * keywordの_がワイルドカードとして解釈されないことのテストです。
     */
    public function test_index_keyword_underscore_is_not_treated_as_wildcard(): void
    {
        Staff::factory()->create(['name' => 'アンダースコア', 'email' => 'a_b@example.com']);
        Staff::factory()->create(['name' => 'エックス', 'email' => 'axb@example.com']);

        $response = $this->get('/api/staffs?keyword=a_b');
        $response->assertStatus(200);
        $data = $response->json('data');
        $this->assertCount(1, $data);
    }

    /**
     * スタッフ権限更新テストです。
     */
    public function test_update_role(): void
    {
        $staff = Staff::factory()->create();
        $params = $this->getRequestParams('Staff/updateRole.json');
        $id = $staff->id;
        $response = $this->withHeader('X-Executor-Id', '1')
            ->patch("/api/staffs/{$id}/updateRole", $params);
        $data = $this->getResponseData('Staff/updateRole.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * スタッフ削除テストです。
     */
    public function test_destroy(): void
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
