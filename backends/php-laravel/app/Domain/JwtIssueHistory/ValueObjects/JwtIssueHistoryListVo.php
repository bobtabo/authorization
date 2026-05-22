<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\JwtIssueHistory\ValueObjects;

use App\Support\ValueObjects\AbstractValueObject;
use Illuminate\Support\Collection;

/**
 * JWT発行履歴一覧ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\JwtIssueHistory\ValueObjects
 */
class JwtIssueHistoryListVo extends AbstractValueObject
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
