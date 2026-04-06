<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Client\Dtos;

use App\Domain\Client\Enums\ClientStatus;
use App\Support\Dtos\AbstractDto;

/**
 * クライアント API 用の入力 DTO です（一覧・詳細・登録・更新・削除で共用します）。
 *
 * 一覧のクエリ `statuses` は {@see statusesFromRequestInput} で `statuses` プロパティに設定してください。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Client\Dtos
 */
class ClientDto extends AbstractDto
{
    public ?int $id = null;

    public ?string $keyword = null;

    public ?string $startFrom = null;

    public ?string $startTo = null;

    /**
     * 一覧検索用の状態コード（複数）。未指定は null（無条件）。
     *
     * @var list<int>|null
     */
    public ?array $statuses = null;

    public ?string $name = null;

    public ?string $identifier = null;

    public ?string $post_code = null;

    public ?string $pref = null;

    public ?string $city = null;

    public ?string $address = null;

    public ?string $building = null;

    public ?string $tel = null;

    public ?string $email = null;

    /**
     * 更新時の状態（リクエストの整数は {@see assign} で Enum に変換されます）。
     */
    public ?ClientStatus $status = null;
}
