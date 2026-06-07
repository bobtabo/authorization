<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Client\Dtos;

use App\Domain\Client\Enums\ClientStatus;
use App\Support\Dtos\PagerDto;

/**
 * クライアントDTOクラスです。
 *
 * 一覧のクエリ `statuses` は {@see statusesFromRequestInput} で `statuses` プロパティに設定してください。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Client\Dtos
 */
class ClientDto extends PagerDto
{
    public ?int $id = null;

    /** {@inheritdoc} */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed
    {
        if (isset($values['sort_type'])) {
            $values['sort_type'] = strtoupper($values['sort_type']);
        }
        return parent::assign($values, $convert, $excludes);
    }
    public ?string $keyword = null;
    public ?string $startFrom = null;
    public ?string $startTo = null;
    public array $statuses = [];
    public ?string $name = null;
    public ?string $identifier = null;
    public ?string $postCode = null;
    public ?string $pref = null;
    public ?string $city = null;
    public ?string $address = null;
    public ?string $building = null;
    public ?string $tel = null;
    public ?string $email = null;
    public ?string $accessToken = null;
    public ?ClientStatus $status = null;
}
