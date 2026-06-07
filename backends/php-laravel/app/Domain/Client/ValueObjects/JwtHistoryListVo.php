<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Client\ValueObjects;

use App\Support\Enums\SortType;
use App\Support\Traits\Getter;
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
    use Getter;

    private Collection $items;
    private int $count = 0;
    private int $offset = 0;
    private int $limit = 0;
    private string $sort = '';
    private SortType $sortType = SortType::NONE;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        return [
            'items'    => $this->items->all(),
            'count'    => $this->count,
            'offset'   => $this->offset,
            'limit'    => $this->limit,
            'sort'     => $this->sort,
            'sortType' => $this->sortType,
        ];
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

    /**
     * 総件数を設定します。
     *
     * @param int $count 総件数
     */
    public function setCount(int $count): void
    {
        $this->count = $count;
    }

    /**
     * ページング情報を設定します。
     *
     * @param int $offset オフセット
     * @param int $limit 取得件数
     * @param string $sort ソート対象
     * @param SortType $sortType ソート順
     */
    public function setPaging(int $offset, int $limit, string $sort, SortType $sortType): void
    {
        $this->offset = $offset;
        $this->limit = $limit;
        $this->sort = $sort;
        $this->sortType = $sortType;
    }
}
