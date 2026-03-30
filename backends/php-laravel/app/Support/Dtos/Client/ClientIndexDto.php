<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Dtos\Client;

use App\Support\Dtos\AbstractDto;

/**
 * クライアント一覧検索の入力 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Dtos\Client
 */
class ClientIndexDto extends AbstractDto
{
    public ?string $keyword = null;

    public ?string $startFrom = null;

    public ?string $startTo = null;

    /**
     * クエリ status（単一・複数）をそのまま受け取ります。サービス側で正規化します。
     *
     * @var mixed
     */
    public mixed $status = null;
}
