<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;

/**
 * クライアント詳細の HTTP レスポンス用オブジェクトです（最終 JSON で項目を絞れます）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class ClientShowResponse extends AbstractResponse
{
    public ?int $id = null;

    public ?string $name = null;

    public ?string $identifier = null;

    public mixed $post_code = null;

    public mixed $pref = null;

    public mixed $city = null;

    public mixed $address = null;

    public mixed $building = null;

    public mixed $tel = null;

    public mixed $email = null;

    public mixed $status = null;

    public mixed $start_at = null;

    public mixed $stop_at = null;

    public mixed $created_at = null;

    public mixed $updated_at = null;

    public mixed $version = null;
}
