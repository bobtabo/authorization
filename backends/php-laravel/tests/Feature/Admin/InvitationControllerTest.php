<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature\Admin;

use App\Infrastructure\Models\Invitation;
use App\Infrastructure\Models\Staff;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Tests\TestCase;

/**
 * 管理招待ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature\Admin
 */
class InvitationControllerTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * 現在の招待URL取得テストです。
     *
     * @return void
     */
    public function testIndex(): void
    {
        Invitation::factory()->create(['token' => 'test-current-token']);
        $response = $this->get('/api/admin/invitation');
        $data = $this->getResponseData('Invitation/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }

    /**
     * 招待URL発行テストです。
     *
     * @return void
     */
    public function testIssue(): void
    {
        Invitation::factory()->create();
        $staff = Staff::factory()->create();

        $response = $this->withStaffCookie($staff->id)
            ->get('/api/admin/invitation/issue');
        $response
            ->assertStatus(200)
            ->assertJsonStructure(['url', 'token']);
    }
}
