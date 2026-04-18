<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\ValueObjects;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;
use Illuminate\Support\Collection;

/**
 * クライアント一覧ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 *
 * @method Collection getClients()
 */
class ClientListVo extends AbstractValueObject
{
    use Getter;

    private Collection $clients;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        return ['items' => $this->clients->all()];
    }

    /**
     * クライアントリストを設定します。
     *
     * @param Collection $list エンティティのコレクション
     */
    public function assignClients(Collection $list): void
    {
        foreach ($list as $entity) {
            $this->clients->add(
                $entity->attributesBySnake()
            );
        }
    }
}
