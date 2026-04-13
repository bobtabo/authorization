<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace Database\Factories;

use App\Infrastructure\Models\Client as Model;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * クライアントFactoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Database\Factories
 */
class ClientFactory extends Factory
{
    /**
     * @var string モデル
     */
    protected $model = Model::class;

    /**
     * テスト用 RSA キーペアキャッシュ（Factory インスタンス内で再利用）。
     *
     * @var array{private_key: string, public_key: string, fingerprint: string}|null
     */
    private static ?array $keyCache = null;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function definition(): array
    {
        $keys = $this->generateKeyPair();

        return [
            'name' => $this->faker->company(),
            'identifier' => $this->faker->unique()->slug(2),
            'post_code' => '100-0001',
            'pref' => '東京都',
            'city' => '千代田区',
            'address' => '千代田1-1-1',
            'building' => null,
            'tel' => '03-0000-0000',
            'email' => $this->faker->unique()->safeEmail(),
            'access_token' => bin2hex(random_bytes(16)),
            'private_key' => $keys['private_key'],
            'public_key' => $keys['public_key'],
            'fingerprint' => $keys['fingerprint'],
            'status' => 1,
            'start_at' => now(),
            'stop_at' => null,
            'created_at' => now(),
            'created_by' => 1,
            'updated_at' => now(),
            'updated_by' => 1,
            'deleted_at' => null,
            'deleted_by' => null,
            'version' => 1,
        ];
    }

    /**
     * RSA キーペアを生成します（テスト実行中は同一キーを再利用）。
     *
     * @return array{private_key: string, public_key: string, fingerprint: string}
     */
    private static function generateKeyPair(): array
    {
        if (self::$keyCache !== null) {
            return self::$keyCache;
        }

        $resource = openssl_pkey_new([
            'digest_alg' => 'sha256',
            'private_key_bits' => 2048,
            'private_key_type' => OPENSSL_KEYTYPE_RSA,
        ]);

        openssl_pkey_export($resource, $privateKey);
        $details = openssl_pkey_get_details($resource);
        $publicKey = $details['key'];
        $fingerprint = hash('sha256', $privateKey);

        self::$keyCache = [
            'private_key' => $privateKey,
            'public_key' => $publicKey,
            'fingerprint' => $fingerprint,
        ];

        return self::$keyCache;
    }
}
