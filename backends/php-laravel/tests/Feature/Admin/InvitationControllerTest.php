<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature\Admin;

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
     * 招待URL発行テストです。
     *
     * @return void
     */
    public function testIssue(): void
    {
        $response = $this->get('/api/admin/invitation/issue');
        $response
            ->assertStatus(200)
            ->assertJsonStructure(['url', 'token']);
    }
}
