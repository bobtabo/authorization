<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\JwtHistory\Dtos;

use App\Support\Dtos\PagerDto;

/**
 * JWT履歴DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\JwtHistory\Dtos
 */
class JwtHistoryDto extends PagerDto
{
    public ?int $clientId = null;

    /** {@inheritdoc} */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed
    {
        if (isset($values['sort_type'])) {
            $values['sort_type'] = strtoupper($values['sort_type']);
        }
        return parent::assign($values, $convert, $excludes);
    }
}
