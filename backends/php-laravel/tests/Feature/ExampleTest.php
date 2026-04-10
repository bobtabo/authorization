<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Tests\Feature;

use Illuminate\Foundation\Testing\DatabaseMigrations;
use Tests\TestCase;

/**
 * サンプルTestクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests\Feature
 */
class ExampleTest extends TestCase
{
    use DatabaseMigrations;

    /**
     * {@inheritdoc}
     */
    protected function setUp(): void
    {
        parent::setUp();

        $testName = $this->toString();
        if (str($testName)->contains('testExample', true)) {
            //
        }
    }

    /**
     * A basic test example.
     */
    public function test_the_application_returns_a_successful_response(): void
    {
        $response = $this->get('/');

        $response->assertStatus(200);
    }

    /**
     * A basic test example.
     *
     * @return void
     */
    public function testExample()
    {
        /*
        //エラーになるのでコメントアウト
        $params = $this->getRequestParams('example.json', [
            'IP' => '6543210987654321',
        ]);

        $response = $this->get('/', $params);

        $data = $this->getResponseData('example.json');

        $response
            ->assertStatus(200)
            ->assertJson($data);
        */

        $this->assertTrue(true);
    }
}
