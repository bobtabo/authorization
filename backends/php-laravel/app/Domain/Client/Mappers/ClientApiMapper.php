<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Mappers;

use App\Domain\Client\Entities\Client;
use App\Domain\Client\Enums\ClientStatus;

/**
 * クライアント Entity を API レスポンス向け配列へ変換します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\Mappers
 */
class ClientApiMapper
{
    /**
     * @return array<string, mixed>
     */
    public static function toResponseArray(Client $client): array
    {
        return [
            'id' => $client->id,
            'name' => $client->name,
            'identifier' => $client->identifer,
            'post_code' => $client->postCode,
            'pref' => $client->pref,
            'city' => $client->city,
            'address' => $client->address,
            'building' => $client->building,
            'tel' => $client->tel,
            'email' => $client->email,
            'status' => $client->status instanceof ClientStatus ? $client->status->value : $client->status,
            'start_at' => $client->startAt?->toIso8601String(),
            'stop_at' => $client->stopAt?->toIso8601String(),
            'created_at' => $client->createdAt?->toIso8601String(),
            'updated_at' => $client->updatedAt?->toIso8601String(),
            'version' => $client->version,
        ];
    }
}
