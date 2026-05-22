<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\ValueObjects;

use App\Support\ValueObjects\AbstractValueObject;
use Illuminate\Support\Collection;

/**
 * JWT履歴一覧ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 */
class JwtHistoryListVo extends AbstractValueObject
{
    private Collection $items;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        return ['items' => $this->items->all()];
    }

    /**
     * エンティティコレクションからアイテムを設定します。
     *
     * @param Collection $list エンティティのコレクション
     */
    public function assignItems(Collection $list): void
    {
        foreach ($list as $entity) {
            $this->items->add($entity->attributesBySnake());
        }
    }
}
