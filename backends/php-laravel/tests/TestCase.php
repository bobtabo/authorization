<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

namespace Tests;

use App\Support\Tests\CreatesApplication;
use Illuminate\Foundation\Testing\TestCase as BaseTestCase;
use Illuminate\Support\Facades\File;
use ReflectionClass;
use ReflectionMethod;
use ReflectionProperty;

/**
 * 基底Testクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Tests
 */
abstract class TestCase extends BaseTestCase
{
    use CreatesApplication;

    /**
     * リフレクションメソッドを取得します。
     *
     * @param string $class クラス名
     * @param string $method メソッド名
     * @return ReflectionMethod メソッド
     * @throws \ReflectionException リフレクションエラー時にスローされる例外です
     */
    protected function reflectionMethod(string $class, string $method): ReflectionMethod
    {
        $method = (new ReflectionClass($class))->getMethod($method);
        $method->setAccessible(true);

        return $method;
    }

    /**
     * リフレクションプロパティを取得します。
     *
     * @param string $class クラス名
     * @param string $property プロパティ名
     * @return ReflectionProperty プロパティ
     * @throws \ReflectionException リフレクションエラー時にスローされる例外です
     */
    protected function reflectionProperty(string $class, string $property): ReflectionProperty
    {
        $prop = (new ReflectionClass($class))->getProperty($property);
        $prop->setAccessible(true);

        return $prop;
    }

    /**
     * メソッドを実行します。
     *
     * @param string $class クラス名
     * @param string $method メソッド名
     * @param array<int, mixed> $args メソッド引数
     * @param bool $useApp クラスのインスタンス生成に app が不要な場合 false を設定します
     * @return mixed メソッド戻り値
     * @throws \ReflectionException リフレクションエラー時にスローされる例外です
     */
    public function executeMethod(string $class, string $method, array $args = [], bool $useApp = true)
    {
        $method = $this->reflectionMethod($class, $method);
        $instance = $useApp ? new $class($this->app) : new $class();
        return $method->invokeArgs($instance, $args);
    }

    /**
     * テスト用 staff_id クッキーを生成します。
     * テストフレームワークが withCookies 送信前に encrypt() するため、平文で渡します。
     *
     * @param int $staffId スタッフID
     * @return array<string, string> クッキー配列
     */
    protected function staffCookies(int $staffId): array
    {
        return ['staff_id' => (string)$staffId];
    }

    /**
     * リクエストパラメータを取得します。
     *
     * @param string $jsonFile データファイル名
     * @param array $mergeData マージデータ連想配列
     * @return array リクエストパラメータ連想配列
     */
    protected function getRequestParams(string $jsonFile, array $mergeData = []): array
    {
        return $this->getTestData('/Feature/Requests/' . $jsonFile, $mergeData);
    }

    /**
     * レスポンスデータを取得します。
     *
     * @param string $jsonFile データファイル名
     * @param array $mergeData マージデータ連想配列
     * @return array リクエストパラメータ連想配列
     */
    protected function getResponseData(string $jsonFile, array $mergeData = []): array
    {
        return $this->getTestData('/Feature/Responses/' . $jsonFile, $mergeData);
    }

    /**
     * テストデータを取得します。
     *
     * @param string $jsonFilePath データファイルパス
     * @param array $mergeData マージデータ連想配列
     * @return array リクエストパラメータ連想配列
     */
    protected function getTestData(string $jsonFilePath, array $mergeData = []): array
    {
        $json = File::get(base_path('tests') . $jsonFilePath, true);
        $result = json_decode($json, true);
        if (!empty($mergeData)) {
            foreach ($mergeData as $key => $value) {
                $result[$key] = $value;
            }
        }
        return $result;
    }
}
