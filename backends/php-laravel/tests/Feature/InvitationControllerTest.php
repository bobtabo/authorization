<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use App\Infrastructure\Models\Invitation;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Tests\TestCase;

/**
 * 招待ControllerTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
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
        $response = $this->get('/api/invitation');
        $data = $this->getResponseData('Invitation/index.json');
        $response
            ->assertStatus(200)
            ->assertJson($data);
    }
}
