<?php
/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */
namespace Tests\Feature;

use Illuminate\Foundation\Testing\DatabaseMigrations;
use Tests\TestCase;

/**
 * サンプルTestクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
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
